(ns leiningen.polylith.cmd.doc.table
  (:require [leiningen.polylith.cmd.shared :as shared]))

(defn max-deps [{:keys [_ _ children]} depth]
  (if (empty? children)
    depth
    (apply max (map #(max-deps % (inc depth)) children))))

(defn count-cols [{:keys [_ _ children]}]
  (cond
    (empty? children) 1
    :else (apply + (map count-cols children))))

(defn count-columns [tree]
  (let [sections (count-cols tree)]
    (if (zero? sections)
      0
      (dec (* 2 sections)))))

(defn calc-table
  ([ws-path top-dir tree]
   (let [maxy (dec (* 2 (max-deps tree 1)))]
     (calc-table ws-path top-dir maxy tree)))
  ([ws-path top-dir maxy tree]
   (let [result (transient (vec (repeat maxy [])))
         comp->ifc (into {} (map #(vector % (shared/interface-of ws-path top-dir %))
                                 (shared/all-components ws-path)))
         _ (calc-table tree comp->ifc 0 maxy result)
         table (vec (reverse (persistent! result)))]
     (mapv #(interpose {:type "spc"} %) table)))
  ([{:keys [entity type children top] :as tree} comp->ifc y maxy result]
   (if (= type "component")
     (let [interface (comp->ifc entity)
           columns (count-columns tree)]
       (assoc! result (inc y) (conj (get result (inc y)) {:entity entity
                                                          :type "component"
                                                          :samename false
                                                          :top (empty? children)
                                                          :bottom false
                                                          :columns columns}))
       (assoc! result y (conj (get result y) {:entity interface
                                              :type "interface"
                                              :samename (= interface entity)
                                              :top false
                                              :bottom (zero? y)
                                              :columns columns})))
     (assoc! result y (conj (get result y) {:entity entity
                                            :type type
                                            :samename false
                                            :top (or top false)
                                            :bottom (zero? y)
                                            :columns (count-columns tree)})))
   (let [nexty (+ y (if (= type "component") 2 1))]
     (if (empty? children)
       (doseq [yy (range nexty maxy)]
         (assoc! result yy (conj (get result yy) {:entity ""
                                                  :type "component"
                                                  :samename false
                                                  :top false
                                                  :bottom false
                                                  :columns 1})))
       (doseq [child children]
         (calc-table child comp->ifc nexty maxy result))))))
