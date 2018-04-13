(ns leiningen.polylith.cmd.test-helper
  (:require [clojure.test :refer :all]
            [leiningen.polylith.cmd.info]
            [leiningen.polylith.file :as file]))

(defn settings [ws-dir top-ns top-dir]
  {:root ws-dir
   :polylith {:vcs "git"
              :build-tool "leiningen"
              :top-dir top-dir
              :top-ns top-ns
              :clojure-version "1.9.0"
              :ignored-tests []
              :example-hash1 "2c851f3c6e7a5114cecf6bdd6e1c8c8aec8b32c1"
              :example-hash2 "58cd8b3106c942f372a40616fe9155c9d2efd122"}
   :top-ns top-ns
   :top-dir top-dir
   :clojure-version "1.9.0"
   :sha1 "2c851f3c6e7a5114cecf6bdd6e1c8c8aec8b32c1"
   :sha2 "58cd8b3106c942f372a40616fe9155c9d2efd122"})

(def diff
  ["bases/base1/src/base1/subsystem/do_stuff.clj"
   "bases/base1/src/base1/subsystem/more_stuff.clj"
   "bases/base2/src/base2/important/core.clj"
   "components/comp1/src/comp1/interface.clj"
   "components/comp2/src/comp2/core.clj"
   "environments/development/test/common"
   "interfaces/src/comp1/interface.clj"
   "interfaces/src/comp2/interface.clj"
   "project.clj"
   "systems/system1/project.clj"])

(def root-dir (atom nil))

(defn test-setup-and-tear-down [f]
  (let [path (str (file/temp-dir) "polylith-root")]
    (if (file/create-dir path)
      (reset! root-dir path)
      (throw (Exception. (str "Could not create directory: " path))))
    (f)
    (file/delete-dir path)))

(use-fixtures :each test-setup-and-tear-down)

(defn content [ws-dir directory]
  (file/read-file (str ws-dir "/" directory)))

(defn interfaces-project-content [name]
  [['defproject name "1.0"
    :description "Component interfaces"
    :dependencies [['org.clojure/clojure "1.9.0"]]
    :aot
    :all]])
