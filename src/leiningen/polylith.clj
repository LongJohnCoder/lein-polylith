(ns leiningen.polylith
  (:require [leiningen.polylith.cmd.add :as add]
            [leiningen.polylith.cmd.build :as build]
            [leiningen.polylith.cmd.changes :as changes]
            [leiningen.polylith.cmd.compile :as compile]
            [leiningen.polylith.cmd.create :as create]
            [leiningen.polylith.cmd.deps :as deps]
            [leiningen.polylith.cmd.diff :as diff]
            [leiningen.polylith.cmd.help :as help]
            [leiningen.polylith.cmd.info :as info]
            [leiningen.polylith.cmd.remove :as remove-from]
            [leiningen.polylith.cmd.settings :as settings]
            [leiningen.polylith.cmd.success :as success]
            [leiningen.polylith.cmd.sync-deps :as sync]
            [leiningen.polylith.cmd.test :as test]
            [clojure.string :as str]))

(defn create-ws? [subtask args]
  (and (= "create" subtask)
       (or
         (= "w" (first args))
         (= "workspace" (first args)))))

(defn ^:no-project-needed polylith
  "Helps you develop component based systems"
  ([project]
   (help/execute []))
  ([project subtask & args]
   (let [ws-path (:root project)
         settings (:polylith project)
         top-ns (:top-namespace settings)
         top-dir (when top-ns (str/replace top-ns #"\." "/"))
         clojure-version (:clojure-version settings "1.9.0")]
     (if (nil? settings)
       (cond
         (= "help" subtask) (help/execute args)
         (create-ws? subtask args) (create/execute ws-path top-dir top-ns clojure-version args)
         :else (println (str "The command must be executed from the workspace root directory.")))
       (case subtask
         "add" (add/execute ws-path top-dir args)
         "build" (build/execute ws-path top-dir args)
         "changes" (changes/execute ws-path top-dir args)
         "compile" (compile/execute ws-path top-dir args)
         "create" (create/execute ws-path top-dir top-ns clojure-version args)
         "deps" (deps/execute ws-path top-dir args)
         "diff" (diff/execute ws-path args)
         "help" (help/execute args)
         "info" (info/execute ws-path top-dir args)
         "remove" (remove-from/execute ws-path top-dir args)
         "settings" (settings/execute ws-path settings args)
         "success" (success/execute ws-path args)
         "sync-deps" (sync/execute ws-path top-dir)
         "test" (test/execute ws-path top-dir args)
         (println (str "Subtask '" subtask "' not found. Type 'lein polylith' for help.")))))))
