#!/usr/bin/env bb

(require '[cheshire.core :as c])

(defn parse-json
  [json]
  (c/parse-string json true))

(defn -main
  [& [[f j & opts]]]
  (when (or (empty? f) (empty? j))
    (println "Usage: <fn> <json>")
    (System/exit 1))
  (let [usrfn  (eval (read-string f))
        json   (parse-json j)
        result (c/generate-string (usrfn json) {:pretty true})]
    (println result)))

(-main *command-line-args*)
