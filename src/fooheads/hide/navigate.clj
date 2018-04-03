(ns fooheads.hide.navigate
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [fooheads.hide.zip :as hz]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.findz :as findz]
            [clojure.repl :as repl]))

(defn get-namespace
  "Returns the namespace (as a symbol) for the given position. 
  Currently only seems to find the top-level namespace.
  Returns nil if no namespace in found."
  [code row col]
  (some-> (hz/zloc code row col) 
          hz/find-namespace
          hz/extract-namespace-sym))

(defn get-filename 
  "Returns the full path for the file contaning the `sym`
  in `namespace`. Returns nil if the symbol can't be resolved
  or if the file can't be found."
  [namespace sym]
  (some->> sym
       (ns-resolve namespace)
       meta
       :file
       io/resource
       .getFile))

(defn find-definition
  "Finds the filename, row and col, where the the symbol under 
  the cursor is defined."
  ([code row col] (find-definition (hz/zloc code row col)))

  ([zloc]
   (let [namespace (-> zloc hz/find-namespace hz/extract-namespace-sym)
         sym (-> zloc z/node :value)
         metadata (meta (ns-resolve namespace sym))
         file-path (some->> metadata :file io/resource .getFile)
         row (:line metadata)
         col (:column metadata)]
     (if file-path
       [file-path row col]))))

(defn get-doc [ns sym]
  (binding [*ns* (find-ns ns) ]
    (eval `(with-out-str (repl/doc ~sym)))))

(defn doc
  ([code row col] (doc (hz/zloc code row col)))

  ([zloc]
   (let [namespace (-> zloc hz/find-namespace hz/extract-namespace-sym)
         sym (-> zloc z/node :value)]
     ; (log/debug "metadata:" metadata)
     (get-doc namespace sym)
     )))


