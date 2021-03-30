(ns dev)

(require 'usermanager.web.main)

(def system (usermanager.web.main/new-system 9000))

(require '[com.stuartsierra.component :as component])

(alter-var-root #'system component/start)

;; test http://localhost:9000

(alter-var-root #'system component/stop)
