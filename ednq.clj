#!/usr/bin/env bb

(require '[cheshire.core :as c])
(require '[babashka.cli :as cli])

(defn json->edn [json-str]
  (c/parse-string json-str true))

(defn edn->json [edn]
  (c/generate-string edn {:pretty true}))

(defn type->edn
  "Convert data of given type to edn."
  [data in-type]
  (cond
    (= :edn in-type)  data
    (= :json in-type) (json->edn data)
    :else             (json->edn data)))

(defn edn->type
  "Convert edn data to given type."
  [data out-type]
  (cond
    (= :json out-type) (edn->json data)
    (= :edn out-type)  data
    :else              (edn->json data)))

(defn transform
  "Transform data using the given Clojure function (in a string) with the given data types.
  - fnstr: Clojure function string
  - data: data to transform
  - in-type: format of given data
  - out-type: format to convert given data to
  Allowed data types:
  :edn, :json"
  [fnstr data in-type out-type]
  (let [f (eval (read-string fnstr))]
    (-> data
        (type->edn in-type)
        f
        (edn->type out-type))))

(def usage
  (str
   "Usage: ednq <fn> <data> <options>\n"
   "\n"
   "Options:\n"
   "-i, --in=TYPE   set the input data type\n"
   "-o, --out=TYPE  set the output data type\n"
   "\n"
   "TYPE can be: json(default), edn"))

(defn -main
  [cli-args]
  (let [{:keys [cmds opts]}  (cli/parse-args cli-args)
        {:keys [i in o out]} opts
        [fnstr data]         cmds
        in-type              (keyword (or i in))
        out-type             (keyword (or o out))
        allowed-types        #{:edn :json}]
    (when (or (empty? data)
              (empty? fnstr)
              (not (contains? allowed-types in-type))
              (not (contains? allowed-types out-type)))
      (println usage)
      (System/exit 1))
    (let [result (transform fnstr data in-type out-type)]
      (println result))))

(-main *command-line-args*)
