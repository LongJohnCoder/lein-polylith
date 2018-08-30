(ns leiningen.polylith.cmd.help.build
  (:require [leiningen.polylith.cmd.shared :as shared]))

(defn help []
  (println "  Builds system artifacts.")
  (println)
  (println "  The following steps are performed:")
  (println "    - checks for circular dependencies and stops if found.")
  (println "    - calculates what components and bases to process based on what has")
  (println "      changed since the last successful build.")
  (println "    - calls 'sync' and makes sure that all dependencies in project.clj")
  (println "      files are in sync and that all systems have all components they need.")
  (println "    - AOT compile changed components, bases and systems to check that they compile")
  (println "      and fulfill workspace interfaces and have all libraries they need.")
  (println "    - runs tests for all bases and components that have been affected by the changes.")
  (println "    - executes build.sh for all changed systems to make sure they have a working")
  (println "      build script and no missing components or libraries.")
  (println "    - if the entire build is successful, then execute the success command")
  (println "      that updates the time for the last successful build.")
  (println)
  (println "  lein polylith build [ARG] [SKIP]")
  (println "    ARG = (omitted) -> Since last successful build, stored in bookmark")
  (println "                       :last-successful-build in WS-ROOT/.polylith/time.edn")
  (println "                       or :last-successful-build in WS-ROOT/.polylith/git.edn")
  (println "                       if you have CI variable set to something on the machine.")
  (println "          timestamp -> Since the given timestamp (milliseconds since 1970).")
  (println "          git-hash  -> Since the given git hash if the CI variable is set.")
  (println "          bookmark  -> Since the timestamp for the given bookmark in")
  (println "                       WS-ROOT/.polylith/time.edn or since the git hash")
  (println "                       for the given bookmark in WS-ROOT/.polylith/git.edn")
  (println "                       if the CI variable is set.")
  (println)
  (println "    SKIP = (omitted)      -> Executes all steps.")
  (println "           -circular-deps -> Skips checking for circular dependencies step")
  (println "           -sync          -> Skips dependency sync step")
  (println "           -compile       -> Skips compilation step")
  (println "           -test          -> Skips test step")
  (println "           -success       -> Skips success step")
  (println)
  (println "  'lein polylith build 0' can be used to build all files in the workspace")
  (println "  (or at least changes since 1970-01-01).")
  (println)
  (println "  examples:")
  (println "    lein polylith build")
  (println "    lein polylith build -compile")
  (if (shared/ci?)
    (println "    lein polylith build 7d7fd132412aad0f8d3019edfccd1e9d92a5a8ae")
    (println "    lein polylith build 1523649477000"))
  (println "    lein polylith build mybookmark")
  (println "    lein polylith build 1523649477000 -compile -test"))

