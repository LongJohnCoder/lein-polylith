(ns leiningen.polylith.cmd.rename-test
  (:require [clojure.test :refer :all]
            [leiningen.polylith.cmd.test-helper :as helper]
            [leiningen.polylith.file :as file]
            [leiningen.polylith :as polylith]))

(use-fixtures :each helper/test-setup-and-tear-down)

(deftest polylith-rename--with-namespace--rename-files-directories-and-symbolic-links
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "my.company")]
      (polylith/polylith nil "create" "w" "ws1" "my.company")
      (polylith/polylith project "create" "s" "system-1")
      (polylith/polylith project "create" "c" "comp-1")
      (polylith/polylith project "add" "comp-1" "system-1")
      (polylith/polylith project "rename" "c" "comp-1" "comp-1b")

      (is (= #{".gitignore"
               ".polylith"
               ".polylith/time.local.edn"
               "Readme.md"
               "bases"
               "bases/system-1"
               "bases/system-1/Readme.md"
               "bases/system-1/project.clj"
               "bases/system-1/resources"
               "bases/system-1/resources/.keep"
               "bases/system-1/resources/system-1"
               "bases/system-1/resources/system-1/.keep"
               "bases/system-1/src"
               "bases/system-1/src/my"
               "bases/system-1/src/my/company"
               "bases/system-1/src/my/company/system_1"
               "bases/system-1/src/my/company/system_1/core.clj"
               "bases/system-1/test"
               "bases/system-1/test/my"
               "bases/system-1/test/my/company"
               "bases/system-1/test/my/company/system_1"
               "bases/system-1/test/my/company/system_1/core_test.clj"
               "components"
               "components/comp-1b"
               "components/comp-1b/Readme.md"
               "components/comp-1b/project.clj"
               "components/comp-1b/resources"
               "components/comp-1b/resources/.keep"
               "components/comp-1b/resources/comp-1b"
               "components/comp-1b/resources/comp-1b/.keep"
               "components/comp-1b/src"
               "components/comp-1b/src/my"
               "components/comp-1b/src/my/company"
               "components/comp-1b/src/my/company/comp_1b"
               "components/comp-1b/src/my/company/comp_1b/core.clj"
               "components/comp-1b/src/my/company/comp_1b/interface.clj"
               "components/comp-1b/test"
               "components/comp-1b/test/my"
               "components/comp-1b/test/my/company"
               "components/comp-1b/test/my/company/comp_1b"
               "components/comp-1b/test/my/company/comp_1b/core_test.clj"
               "environments"
               "environments/development"
               "environments/development/docs"
               "environments/development/docs/comp-1b-Readme.md"
               "environments/development/docs/system-1-Readme.md"
               "environments/development/interfaces"
               "environments/development/interfaces/my"
               "environments/development/interfaces/my/company"
               "environments/development/interfaces/my/company/comp_1"
               "environments/development/interfaces/my/company/comp_1/interface.clj"
               "environments/development/project-files"
               "environments/development/project-files/bases"
               "environments/development/project-files/bases/system-1-project.clj"
               "environments/development/project-files/components"
               "environments/development/project-files/components/comp-1b-project.clj"
               "environments/development/project-files/interfaces-project.clj"
               "environments/development/project-files/systems"
               "environments/development/project-files/systems/system-1-project.clj"
               "environments/development/project-files/workspace-project.clj"
               "environments/development/project.clj"
               "environments/development/resources"
               "environments/development/resources/.keep"
               "environments/development/resources/comp-1"
               "environments/development/resources/system-1"
               "environments/development/resources/system-1/.keep"
               "environments/development/src"
               "environments/development/src/my"
               "environments/development/src/my/company"
               "environments/development/src/my/company/comp_1b"
               "environments/development/src/my/company/comp_1b/core.clj"
               "environments/development/src/my/company/comp_1b/interface.clj"
               "environments/development/src/my/company/system_1"
               "environments/development/src/my/company/system_1/core.clj"
               "environments/development/test"
               "environments/development/test/my"
               "environments/development/test/my/company"
               "environments/development/test/my/company/comp_1b"
               "environments/development/test/my/company/comp_1b/core_test.clj"
               "environments/development/test/my/company/system_1"
               "environments/development/test/my/company/system_1/core_test.clj"
               "interfaces"
               "interfaces/project.clj"
               "interfaces/src"
               "interfaces/src/my"
               "interfaces/src/my/company"
               "interfaces/src/my/company/comp_1"
               "interfaces/src/my/company/comp_1/interface.clj"
               "logo.png"
               "project.clj"
               "systems"
               "systems/system-1"
               "systems/system-1/Readme.md"
               "systems/system-1/build.sh"
               "systems/system-1/project.clj"
               "systems/system-1/resources"
               "systems/system-1/resources/.keep"
               "systems/system-1/resources/comp-1b"
               "systems/system-1/resources/comp-1b/.keep"
               "systems/system-1/resources/system-1"
               "systems/system-1/resources/system-1/.keep"
               "systems/system-1/src"
               "systems/system-1/src/my"
               "systems/system-1/src/my/company"
               "systems/system-1/src/my/company/comp_1b"
               "systems/system-1/src/my/company/comp_1b/core.clj"
               "systems/system-1/src/my/company/comp_1b/interface.clj"
               "systems/system-1/src/my/company/system_1"
               "systems/system-1/src/my/company/system_1/core.clj"}
             (set (file/relative-paths ws-dir)))))))

(deftest polylith-rename--without-namespace--rename-files-directories-and-symbolic-links
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "")]
      (polylith/polylith nil "create" "w" "ws1" "")
      (polylith/polylith project "create" "c" "comp-1")
      (polylith/polylith project "rename" "c" "comp-1" "comp-1b")

      (is (= #{".gitignore"
               ".polylith"
               ".polylith/time.local.edn"
               "Readme.md"
               "bases"
               "components"
               "components/comp-1b"
               "components/comp-1b/Readme.md"
               "components/comp-1b/project.clj"
               "components/comp-1b/resources"
               "components/comp-1b/resources/.keep"
               "components/comp-1b/resources/comp-1b"
               "components/comp-1b/resources/comp-1b/.keep"
               "components/comp-1b/src"
               "components/comp-1b/src/comp_1b"
               "components/comp-1b/src/comp_1b/core.clj"
               "components/comp-1b/src/comp_1b/interface.clj"
               "components/comp-1b/test"
               "components/comp-1b/test/comp_1b"
               "components/comp-1b/test/comp_1b/core_test.clj"
               "environments"
               "environments/development"
               "environments/development/docs"
               "environments/development/docs/comp-1b-Readme.md"
               "environments/development/interfaces"
               "environments/development/interfaces/comp_1"
               "environments/development/interfaces/comp_1/interface.clj"
               "environments/development/project-files"
               "environments/development/project-files/bases"
               "environments/development/project-files/components"
               "environments/development/project-files/components/comp-1b-project.clj"
               "environments/development/project-files/interfaces-project.clj"
               "environments/development/project-files/systems"
               "environments/development/project-files/workspace-project.clj"
               "environments/development/project.clj"
               "environments/development/resources"
               "environments/development/resources/.keep"
               "environments/development/resources/comp-1"
               "environments/development/src"
               "environments/development/src/comp_1b"
               "environments/development/src/comp_1b/core.clj"
               "environments/development/src/comp_1b/interface.clj"
               "environments/development/test"
               "environments/development/test/comp_1b"
               "environments/development/test/comp_1b/core_test.clj"
               "interfaces"
               "interfaces/project.clj"
               "interfaces/src"
               "interfaces/src/comp_1"
               "interfaces/src/comp_1/interface.clj"
               "logo.png"
               "project.clj"
               "systems"}
             (set (file/relative-paths ws-dir)))))))
