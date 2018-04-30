(ns leiningen.polylith.cmd.info-test
  (:require [clojure.test :refer :all]
            [leiningen.polylith.cmd.info :as info]
            [leiningen.polylith.cmd.test-helper :as helper]
            [leiningen.polylith.file :as file]
            [leiningen.polylith :as polylith]))

(use-fixtures :each helper/test-setup-and-tear-down)

(deftest indirect-entity-changes--depends-on-something-that-depends-on-a-changed-component--returns-true
  (let [all-deps {"user" #{"email"}
                  "email" #{"common"}
                  "common" #{}}]
    (is (= [true]
           (info/indirect-entity-changes "user" #{"user"} all-deps #{"common"})))))

(deftest polylith-info--workspace-with-namespace--return-list-empty-namespace
  (with-redefs [file/current-path (fn [] @helper/root-dir)
                leiningen.polylith.cmd.diff/do-diff (fn [_ _] helper/diff)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "my.company")
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "my.company")
                   (polylith/polylith project "info"))]
      (is (= (str "interfaces:\n"
                  "components:\n"
                  "bases:\n"
                  "systems:\n"
                  "environments:\n"
                  "  development\n")
             output)))))

(deftest polylith-info--workspace-with-namespace--return-list-with-change-information
  (with-redefs [file/current-path (fn [] @helper/root-dir)
                leiningen.polylith.cmd.diff/do-diff (fn [_ _] helper/diff)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "my.company")
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "my.company")
                   (polylith/polylith (helper/settings ws-dir "my.company")
                                      "create" "c" "comp1" "ifc1")
                   (polylith/polylith (helper/settings ws-dir "my.company")
                                      "create" "c" "component2")
                   (polylith/polylith (helper/settings ws-dir "my.company")
                                      "create" "s" "sys1" "sys")
                   (polylith/polylith project "info"))]
      (is (= (str "interfaces:\n"
                  "  component2\n"
                  "  ifc1 *\n"
                  "components:\n"
                  "  comp1 *      > ifc1\n"
                  "  component2\n"
                  "bases:\n"
                  "  sys\n"
                  "systems:\n"
                  "  sys1\n"
                  "    sys   -> base\n"
                  "environments:\n"
                  "  development\n"
                  "    comp1 *      -> component\n"
                  "    component2   -> component\n"
                  "    sys          -> base\n")
             output)))))

(deftest polylith-info--workspace-without-namespace--return-list-with-change-information
  (with-redefs [file/current-path (fn [] @helper/root-dir)
                leiningen.polylith.cmd.diff/do-diff (fn [_ _] helper/diff)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "")
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "")
                   (polylith/polylith (helper/settings ws-dir "")
                                      "create" "c" "comp1")
                   (polylith/polylith (helper/settings ws-dir "")
                                      "create" "s" "sys1" "sys")
                   (polylith/polylith project "info"))]
      (is (= (str "interfaces:\n"
                  "  comp1 *\n"
                  "components:\n"
                  "  comp1 *\n"
                  "bases:\n"
                  "  sys\n"
                  "systems:\n"
                  "  sys1\n"
                  "    sys   -> base\n"
                  "environments:\n"
                  "  development\n"
                  "    comp1 *   -> component\n"
                  "    sys       -> base\n")
             output)))))
