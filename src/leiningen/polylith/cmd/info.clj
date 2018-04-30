(ns leiningen.polylith.cmd.info
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [leiningen.polylith.cmd.diff :as diff]
            [leiningen.polylith.cmd.deps :as deps]
            [leiningen.polylith.file :as file]
            [leiningen.polylith.time :as time]
            [leiningen.polylith.cmd.shared :as shared]))

(defn changed-dirs [dir file-paths]
  (let [n (count (str/split dir #"/"))
        nidx #(nth % n)
        f #(and (str/starts-with? % (str dir "/"))
                (> (count (str/split % #"/")) 2))]
    (vec (sort (set (map #(nidx (str/split % #"/"))
                         (filter f file-paths)))))))

(defn changed-base? [ws-path path changed-bases]
  (let [bases-path (str ws-path "/bases")
        base? (str/starts-with? path bases-path)
        changed? (and
                   base?
                   (let [base (second (str/split (subs path (count bases-path)) #"/"))]
                     (contains? (set changed-bases) base)))]
    {:base?    base?
     :changed? changed?}))

(defn changed-component? [ws-path path changed-components changed-entities-by-ref]
  (let [components-path (str ws-path "/components")
        component? (str/starts-with? path components-path)
        component (second (str/split (subs path (count components-path)) #"/"))
        [changed? changed-by-ref?] [(and component? (contains? (set changed-components) component))
                                    (and component? (contains? changed-entities-by-ref component))]]
    {:component? component?
     :changed?   changed?
     :changed-by-ref? changed-by-ref?}))

(defn changed? [ws-path file changed-bases changed-components changed-entities-by-ref]
  (let [path (file/file->real-path file)
        changed-base (changed-base? ws-path path changed-bases)
        changed-component (changed-component? ws-path path changed-components changed-entities-by-ref)]
    {:name     (file/path->dir-name path)
     :type     (cond
                 (:base? changed-base) "-> base"
                 (:component? changed-component) "-> component"
                 :else "?")
     :changed? (cond
                 (:base? changed-base) (:changed? changed-base)
                 (:component? changed-component) (:changed? changed-component)
                 :else false)
     :changed-by-ref? (cond
                        (:base? changed-base) (:changed-by-ref? changed-base)
                        (:component? changed-component) (:changed-by-ref? changed-component)
                        :else false)}))

(defn system-links [ws-path top-dir system changed-bases changed-components changed-entities-by-ref]
  (let [dir (if (zero? (count top-dir)) "/src" (str "/src/" top-dir))]
    (mapv #(changed? ws-path % changed-bases changed-components changed-entities-by-ref)
          (file/directories (str ws-path "/systems/" system dir)))))

(defn systems-info [ws-path top-dir systems changed-bases changed-components changed-entities-by-ref]
  (into {} (mapv (juxt identity #(system-links ws-path top-dir % changed-bases changed-components changed-entities-by-ref)) systems)))

(defn any-changes? [systems-info system]
  (or (some true? (map :changed? (systems-info system))) false))

(defn base-or-component-changed? [systems-info changed-systems]
  (let [base-changes (map (juxt identity #(any-changes? systems-info %)) (keys systems-info))]
    (map (juxt first #(or (last %) (contains? changed-systems (first %)))) base-changes)))

(defn system-entity [path]
  (let [parts (str/split path #"/")]
    (when (and
            (= "systems" (first parts))
            (or (< (count parts) 3)
                (not= "src" (nth parts 2))))
      (second parts))))

(defn all-changed-systems-dir [paths]
  (set (filter identity (map system-entity paths))))

(defn all-environments [ws-path]
  (sort (file/directory-names (str ws-path "/environments"))))

(defn changed-interfaces
  ([ws-path paths top-dir]
   (changed-interfaces paths (shared/all-interfaces ws-path top-dir)))
  ([paths interfaces]
   ;; todo: also check "interfaces/test".
   (set (filter interfaces (changed-dirs "interfaces/src" paths)))))

(defn changed-components
  ([ws-path paths]
   (changed-components nil paths (shared/all-components ws-path)))
  ([_ paths components]
   (set (filter components (changed-dirs "components" paths)))))

(defn changed-bases
  ([ws-path paths]
   (changed-bases nil paths (shared/all-bases ws-path)))
  ([_ paths bases]
   (set (filter bases (set (changed-dirs "bases" paths))))))

(defn indirect-entity-changes [entity disallowed-deps all-deps changed-entities]
  (if (contains? changed-entities entity)
    [false]
    (let [deps (all-deps entity)]
      (if (empty? deps)
        [false]
        (if (empty? (set/intersection deps disallowed-deps))
          (if (some (comp not nil?) (map changed-entities deps))
            [true]
            (let [values (mapv #(indirect-entity-changes % (conj disallowed-deps %) all-deps changed-entities) deps)]
              [(true? (some true? (map first values)))]))
          [false "recursive dependencies"])))))

(defn environment-links [ws-path top-dir environment changed-bases changed-components changed-entities-by-ref]
  (let [dir (str ws-path "/environments/" environment "/src/" (shared/full-name top-dir "/" ""))]
    (sort-by :name
             (mapv #(changed? ws-path % changed-bases changed-components changed-entities-by-ref)
                   (file/directories dir)))))

(defn environments-info [ws-path top-dir environments changed-bases changed-components changed-entities-by-ref]
  (into {} (mapv (juxt identity #(environment-links ws-path top-dir % changed-bases changed-components changed-entities-by-ref)) environments)))

(defn ->interface-component [ws-path top-dir component interfaces]
  [(shared/interface-of ws-path top-dir component interfaces) component])

(defn ->changed [[entity [changed]]]
  [entity changed])

(defn environment-deps [ws-path top-dir interfaces fn-deps levels changed-entities [environment infos]]
  (let [entities (set (map :name infos))
        ifc->component (into {} (filterv first
                                         (map #(->interface-component ws-path top-dir % interfaces)
                                              entities)))
        dependencies (into {} (map #(vector % (set (deps/component-deps fn-deps % levels ifc->component))) entities))
        changes-info (mapv #(vector % (indirect-entity-changes % #{%} dependencies changed-entities)) entities)]
    (into {} (map ->changed changes-info))))

(defn environments-deps [ws-path top-dir interfaces fn-deps levels changed-entities env-infos]
  (let [deps (map #(environment-deps ws-path top-dir interfaces fn-deps levels changed-entities %)
                  env-infos)]
    (set (map first (into {} (mapv #(filter second %) deps))))))

(defn all-indirect-changes [ws-path top-dir paths]
  (let [systems (shared/all-systems ws-path)
        interfaces (shared/all-interfaces ws-path top-dir)
        components (shared/all-components ws-path)
        environments (all-environments ws-path)
        bases (shared/all-bases ws-path)
        ch-bases (changed-bases nil paths bases)
        ch-components (changed-components nil paths components)
        changed-entities (set (concat ch-bases ch-components))
        info (systems-info ws-path top-dir systems ch-bases ch-components #{})
        env (environments-info ws-path top-dir environments ch-bases ch-components #{})
        levels (deps/ns-levels top-dir)
        fn-deps (deps/function-dependencies ws-path top-dir)]
    (set (concat (environments-deps ws-path top-dir interfaces fn-deps levels changed-entities env)
                 (environments-deps ws-path top-dir interfaces fn-deps levels changed-entities info)))))

(defn changed-systems
  ([ws-path paths top-dir]
   (changed-systems (systems-info ws-path
                                  top-dir
                                  (shared/all-systems ws-path)
                                  (changed-bases ws-path paths)
                                  (changed-components ws-path paths)
                                  (all-indirect-changes ws-path top-dir paths))
                    (all-changed-systems-dir paths)))
  ([systems-info changed-system-dirs]
   (mapv first (filter second (base-or-component-changed? systems-info (set changed-system-dirs))))))

(defn info [ws-path top-dir timestamp]
   (let [paths (mapv second (diff/do-diff ws-path timestamp))
         interfaces (shared/all-interfaces ws-path top-dir)
         systems (shared/all-systems ws-path)
         components (shared/all-components ws-path)
         bases (shared/all-bases ws-path)
         environments (all-environments ws-path)
         ch-interfaces (changed-interfaces paths interfaces)
         ch-systems (changed-systems ws-path paths top-dir)
         ch-components (changed-components nil paths components)
         ch-entities-by-ref (all-indirect-changes ws-path top-dir paths)
         ch-bases (changed-bases nil paths bases)]
     {:interfaces              (-> interfaces sort vec)
      :systems                 (-> systems sort vec)
      :components              (-> components sort vec)
      :bases                   (-> bases sort vec)
      :diff                    paths
      :changed-interfaces      ch-interfaces
      :changed-systems         ch-systems
      :changed-components      ch-components
      :changed-bases           ch-bases
      :changed-systems-dir     (all-changed-systems-dir paths)
      :changed-entities-by-ref ch-entities-by-ref
      :systems-info            (systems-info ws-path top-dir systems ch-bases ch-components ch-entities-by-ref)
      :environments-info       (environments-info ws-path top-dir environments ch-bases ch-components ch-entities-by-ref)}))

(defn print-entity
  ([spaces entity changes]
   (let [changed? (contains? changes entity)
         star (if changed? " *" "")]
     (println (str spaces entity star))))
  ([spaces entity type maxlength changed? changed-by-ref?]
   (let [star (if changed? " *" (if changed-by-ref? " (*)" ""))
         star-spaces (str/join (repeat (- maxlength (count (str entity star))) " "))
         string (str spaces entity star star-spaces type)]
     (println string))))

(defn max-length [entities]
  (let [name-counts (map #(+ 3 (count (:name %)) (if (:changed? %) 2 0))
                         (mapcat second entities))]
    (if (empty? name-counts)
      150
      (apply max name-counts))))

(defn component-length [component changed-components]
  (let [length (count component)]
    (if (contains? changed-components component)
      (+ length 2)
      length)))

(defn components-max-length [components changed-components]
  (if (empty? components)
    150
    (apply max (mapv #(component-length % changed-components) components))))

(def type->sort {"-> interface" 1
                 "-> component" 2
                 "-> base" 3})

(defn info-sorting [{:keys [name type]}]
  (str (type->sort type) name))

(defn print-info [{:keys [interfaces
                          components
                          bases
                          changed-interfaces
                          changed-bases
                          changed-components
                          changed-entities-by-ref
                          changed-systems-dir
                          systems-info
                          environments-info]}
                  component->interface]
  (let [systems (-> systems-info keys sort)
        comp-max-length (components-max-length components changed-components)
        systems-max-length (max-length systems-info)
        environments-maxlength (max-length environments-info)]

    (println "interfaces:")
    (doseq [interface interfaces]
      (print-entity "  " interface changed-interfaces))

    (println "components:")
    (doseq [component components]
      (let [interface (component->interface component)
            changed? (contains? changed-components component)
            indirecty-changed? (contains? changed-entities-by-ref component)]
        (print-entity "  " component interface comp-max-length changed? indirecty-changed?)))

    (println "bases:")
    (doseq [base bases]
      (print-entity "  " base changed-bases))

    (println "systems:")
    (doseq [system systems]
      (let [infos (sort-by info-sorting (systems-info system))]
        (when (or (-> infos empty? not)
                  (contains? changed-systems-dir system))
          (print-entity "  " system changed-systems-dir))
        (doseq [{:keys [name type changed? changed-by-ref?]} infos]
          (print-entity "    " name type systems-max-length changed? changed-by-ref?))))

    (println "environments:")
    (doseq [[name info-data] environments-info]
      (let [info (sort-by info-sorting info-data)]
        (println " " name)
        (doseq [{:keys [name type changed? changed-by-ref?]} info]
          (when (or (contains? (set components) name)
                    (contains? (set bases) name))
            (print-entity "    " name type environments-maxlength changed? changed-by-ref?)))))))

(defn component-interface [ws-path top-dir component]
  (let [interface (shared/interface-of ws-path top-dir component)]
    (if (= component interface)
      [component ""]
      [component (str "   > " interface)])))

(defn execute [ws-path top-dir args]
  (let [[_ timestamp] (time/parse-time-args ws-path args)
        data (info ws-path top-dir timestamp)
        component->interface (into {} (map #(component-interface ws-path top-dir %) (data :components)))]
    (print-info data component->interface)))
