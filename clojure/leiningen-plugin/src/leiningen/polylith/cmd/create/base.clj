(ns leiningen.polylith.cmd.create.base
  (:require [leiningen.polylith.cmd.create.shared :as shared]
            [leiningen.polylith.file :as file]))

(defn create-base [ws-path top-dir top-ns base clojure-version clojure-spec-version]
  (let [base-dir (str ws-path "/bases/" base)
        proj-dir (shared/full-name top-dir "/" base)

        src-dir (shared/full-name top-dir "/" (str base-dir "/src/" base))
        ;base-test-dir (str base-dir "/test/" base)
        base-readme-content [(str "# " base)]
        proj-ns (shared/full-name top-ns "/" base)
        ns-name (shared/full-name top-ns "." base)
        interfaces-dependencies (shared/full-name top-ns "/" "interfaces")
        base-project-content [(str "(defproject " proj-ns " \"0.1\"")
                              (str "  :description \"A " base " base\"")
                              (str "  :dependencies [[" interfaces-dependencies " \"1.0\"]")
                              (str "                 " (shared/->dependency "org.clojure/clojure" clojure-version))
                              (str "                 " (shared/->dependency "org.clojure/spec" clojure-spec-version) "]")
                              (str "  :aot :all)")]
        core-content [(str "(ns " ns-name ".core")
                      "  (:gen-class))"
                      ""
                      ";; A stand alone base example. Change to the right type of base."
                      "(defn -main [& args]"
                      "  (println \"Hello world!\"))"]
        test-content [(str "(ns " ns-name ".core-test)")
                      ""
                      ";; Add tests here..."]]

    (file/create-dir base-dir)
    (shared/create-src-dirs! ws-path (str "bases/" base "/src") [proj-dir])
    (shared/create-src-dirs! ws-path (str "bases/" base "/test") [proj-dir])

    (file/create-file (str base-dir "/Readme.md") base-readme-content)
    (file/create-file (str base-dir "/project.clj") base-project-content)
    (file/create-file (str base-dir "/src/" proj-dir "/core.clj") core-content)
    (file/create-file (str base-dir "/test/" proj-dir "/core_test.clj") test-content)))
