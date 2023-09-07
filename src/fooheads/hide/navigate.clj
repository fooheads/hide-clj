(ns fooheads.hide.navigate
  (:require
    [clojure.java.io :as io]
    [clojure.repl :as repl]
    [fooheads.hide.zip :as hz]
    [rewrite-clj.zip :as z]))


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


(defn jar-path [metadata-path]
  (when-let [[_ jar-file-path clojure-file-path]
             (some->>
               metadata-path io/resource .getFile
               (re-matches #"file:(.*\.jar)!/(.*)"))]
    (when (and jar-file-path clojure-file-path)
      (format "zipfile:%s::%s" jar-file-path clojure-file-path))))


(defn file-system-path [metadata-path]
  (when (.exists (io/as-file metadata-path))
    metadata-path))


(defn uri-path [metadata-path]
  (when-let [file-path (some->> metadata-path io/resource .getFile)]
    file-path))

;;; TODO: vim specific paths. Should that path be in hide.nvim?
(defn metadata-path->edit-path
  "Takes a path from clojure metadata :file and converts it
  into an edit path that vim can open. Returns nil if the file
  can't be located"
  [metadata-path]
  (when metadata-path
    (or (jar-path metadata-path)
        (uri-path metadata-path)
        (file-system-path metadata-path))))


(defn find-definition
  "Finds the filename, row and col, where the the symbol under
  the cursor is defined."
  ([code row col] (find-definition (hz/zloc code row col)))

  ([zloc]
   (let [nspace (-> zloc hz/find-namespace hz/extract-namespace-sym)
         sym (-> zloc z/node :value)

         resolved-sym
         (cond
           ; Resolve in the namespace at the current position.
           (and nspace sym)
           (ns-resolve nspace sym)

           ; If not, see if it's a qualified symbol. Can even resolve from
           ; edn-files with this option.
           (qualified-symbol? sym)
           (ns-resolve (symbol (namespace sym)) (symbol (name sym)))

           ; Couldn't resolve, give up.
           :else nil)

         metadata (meta resolved-sym)
         edit-path (metadata-path->edit-path (:file metadata))
         row (:line metadata)
         col (:column metadata)]

     (when edit-path
       [edit-path row col]))))


(defn get-doc [ns sym]
  (when (and ns sym)
    (let [code (if ns
                 (binding [*ns* (find-ns ns)]
                   `(with-out-str (repl/doc ~sym)))
                 `(with-out-str (repl/doc ~sym)))]
      (when code
        (eval code)))))


(defn doc
  ([code row col] (doc (hz/zloc code row col)))

  ([zloc]
   (let [nspace (-> zloc hz/find-namespace hz/extract-namespace-sym)
         sym (-> zloc z/node :value)
         result (get-doc nspace sym)]
     result)))


(comment
  (let [paths ["fooheads/hide_nvim/joho.clj"
               "fooheads/hide_nvim/client2.clj"]]
    (map #((juxt jar-path uri-path file-system-path) %) paths))

  (let [path "fooheads/hide_nvim/joho.clj"]
    ((juxt jar-path uri-path file-system-path) path))

  (let [path "fooheads/hide_nvim/client2.clj"]
    ((juxt jar-path uri-path file-system-path) path))

  (let [path "clojure/core.clj"]
    ((juxt jar-path uri-path file-system-path) path))

  (metadata-path->edit-path "fooheads/hide_nvim/joho.clj")
  (metadata-path->edit-path "fooheads/hide_nvim/client2.clj")
  (metadata-path->edit-path "clojure/core.clj")
  (metadata-path->edit-path "clojure/core.clj")
  (metadata-path->edit-path "fooheads/hide_nvim/client2.clj")
  (metadata-path->edit-path "/Users/nicke/w/fooheads/hide-clj.nvim/src/fooheads/hide_nvim/client2.clj"))


