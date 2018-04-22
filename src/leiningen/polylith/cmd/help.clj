(ns leiningen.polylith.cmd.help
  (:require [leiningen.polylith.version :as v]
            [leiningen.polylith.cmd.help.add :as add]
            [leiningen.polylith.cmd.help.build :as build]
            [leiningen.polylith.cmd.help.changes :as changes]
            [leiningen.polylith.cmd.help.compile :as compile]
            [leiningen.polylith.cmd.help.create :as create]
            [leiningen.polylith.cmd.help.deps :as deps]
            [leiningen.polylith.cmd.help.diff :as diff]
            [leiningen.polylith.cmd.help.info :as info]
            [leiningen.polylith.cmd.help.settings :as settings]
            [leiningen.polylith.cmd.help.success :as success]
            [leiningen.polylith.cmd.help.test :as test-cmd]
            [leiningen.polylith.cmd.help.test-and-build :as test-and-build]))

(defn help []
  (println (str "Polylith " v/version " (" v/date ") - https://github.com/tengstrand/polylith"))
  (println)
  (println "  lein polylith CMD [ARGS]  - where CMD [ARGS] are:")
  (println)
  (println "    add C S              Adds a component to a system.")
  (println "    build N [A]          Build changed systems and create artifacts.")
  (println "    changes E P [A]      List changed components, bases and systems.")
  (println "    compile P [A]        Compile changed components, bases and systems.")
  (println "    create X N [A]       Creates component or workspace.")
  (println "    deps [f]             List dependencies.")
  (println "    diff P [A] [F]       List all changes since a specific point in time.")
  (println "    info P [A]           List interfaces, components, bases and systems.")
  (println "    settings P           The polylith settings in current project.clj.")
  (println "    success P            Sets last-successful-build time.")
  (println "    test P [A]           Execute tests in changed components and bases.")
  (println "    test-and-build P [A] Execute tests in changed components, build changed systems, and set las-successful-build time.")
  (println)
  (println "  lein polylith [help]        Show this help.")
  (println "  lein polylith help CMD      Show help for a specific command.")
  (println "  lein polylith help project  Show valid project.clj settings.")
  (println)
  (println "  Examples:")
  (println "    lein polylith add mycomponent mysystem")
  (println "    lein polylith build")
  (println "    lein polylith build local")
  (println "    lein polylith build local 1523649477000")
  (println "    lein polylith build local mybookmark")
  (println "    lein polylith changes b")
  (println "    lein polylith changes b local")
  (println "    lein polylith changes c local 1523649477000")
  (println "    lein polylith changes s local mybookmark")
  (println "    lein polylith compile")
  (println "    lein polylith compile local")
  (println "    lein polylith compile local 1523649477000")
  (println "    lein polylith compile local mybookmark")
  (println "    lein polylith create c mycomponent")
  (println "    lein polylith create c mycomponent myinterface")
  (println "    lein polylith create s mysystem")
  (println "    lein polylith create s mysystem mybase")
  (println "    lein polylith create w myworkspace -")
  (println "    lein polylith create w myworkspace com.my.company")
  (println "    lein polylith deps")
  (println "    lein polylith deps f")
  (println "    lein polylith diff")
  (println "    lein polylith diff local")
  (println "    lein polylith diff local 1523649477000")
  (println "    lein polylith diff local mybookmark")
  (println "    lein polylith diff local mybookmark +")
  (println "    lein polylith help info")
  (println "    lein polylith info")
  (println "    lein polylith info 1523649477000")
  (println "    lein polylith info mybookmark")
  (println "    lein polylith settings")
  (println "    lein polylith settings local")
  (println "    lein polylith success")
  (println "    lein polylith success local")
  (println "    lein polylith test")
  (println "    lein polylith test local")
  (println "    lein polylith test local 1523649477000")
  (println "    lein polylith test local mybookmark")
  (println "    lein polylith test-and-build")
  (println "    lein polylith test-and-build local")
  (println "    lein polylith test-and-build local 1523649477000")
  (println "    lein polylith test-and-build local mybookmark"))

(defn project []
  (println "  These are the valid settings of the :polylith section in the developments")
  (println "  project.clj file (the main development project if having more than one):")
  (println)
  (println "    :top-namespace x           x is the name of the top namespace. This namespaces is added")
  (println "                               to the interfaces project.clj and each component project.clj")
  (println "                               file (to add the correct Maven artifact namespace).")
  (println)
  (println "    :clojure-version x         x is the version of clojure used when creating components.")
  (println)
  (println "  Example of project.clj:")
  (println "    (defproject ...")
  (println "      ...")
  (println "      :polylith {:top-namespace \"com.mycompany\"")
  (println "                 :clojure-version \"1.9.0\"}")
  (println "      ...")
  (println "    )"))

(defn execute [[cmd]]
  (condp = cmd
    "add" (add/help)
    "build" (build/help)
    "changes" (changes/help)
    "compile" (compile/help)
    "create" (create/help)
    "deps" (deps/help)
    "diff" (diff/help)
    "info" (info/help)
    "project" (project)
    "settings" (settings/help)
    "success" (success/help)
    "test" (test-cmd/help)
    "test-and-build" (test-and-build/help)
    (help)))
