(ns fooheads.hide.user)


(defonce ^:private doc
  (atom {}))


(comment
  (deref doc))


(defn set-doc
  "Sets a user defined docidden with a map from a identifier (keyword or symbol,
  namespaced or not) to a string (multiline or not), that represents
  the documentation for that element."
  [m]
  (reset! doc m))


(defn get-doc [k]
  (get @doc k))

