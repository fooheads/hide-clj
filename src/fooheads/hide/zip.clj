(ns fooheads.hide.zip
  (:require
    [rewrite-clj.zip :as z]
    [rewrite-clj.zip.findz :as findz]))


;;
;; Predicates
;;

(defn not-whitespace?
  "Returns true if `zloc` is not a whitespace node."
  [zloc]
  (not= (z/tag zloc) :whitespace))


(defn ns?
  "Returns `true` if zloc is positioned on a list node where
  the first value is the symbol 'ns."
  [zloc]
  (and (= (z/tag zloc) :list)
       (= (-> zloc z/sexpr first) 'ns)))


(defn sym?
  "Returns `true` if zloc is positioned on a token node where
  the value is a symbol"
  [zloc]
  (and (= (z/tag zloc) :token)
       (symbol? (-> (z/node zloc) :value))))

;;
;; Utilities
;;

(defn extract-namespace-sym
  "Extracts the namespace symbol from a zloc positioned
  on the namespace form."
  [zloc]
  (some-> zloc z/sexpr second))
          ;(z/down)
          ;(z/find-next sym?)
          ;(z/node)
          ;:value))


;;
;; Navigation functions
;;

(defn up-or-left
  "Goes up if it's not at the top level, otherwise goes left."
  [zloc]
  (if (-> zloc z/up :node)
    (z/up zloc)
    (z/left zloc)))


(defn left-or-up
  "Goes left if it's not at the left most element level, otherwise goes up"
  [zloc]
  (if (-> zloc z/left :node)
    (z/left zloc)
    (z/up zloc)))


(defn find-namespace
  "Finds the namespace node from the `zloc`.
  Currently only seems to find the top-level namespace.
  Returns nil if no namespace can be found."
  [zloc]
  (z/find zloc left-or-up ns?))

;;
;; Entry point
;;

(defn zloc
  "Creates a zipper from the code and positions zloc at
  a position in the tree that matches (`row` `col`)."
  [code row col]
  (let [pos {:row row :col col :end-row row :end-col col}
        zipper (z/of-string code {:track-position? true})]
    (findz/find-last-by-pos zipper pos not-whitespace?)))


(comment
  (def loc
    (z/of-string
      ;(slurp "test_project/src/example_0.clj")
      (slurp "test_project/src/example.clj")
      ;(slurp "test_project/src/example_3.cljc")
      ;"(ns example-0\n  \"Example namespace\")"
      {:track-position? true}))

  (-> loc
      (z/find-last-by-pos {:row 5 :col 7 :end-row 5 :end-col 7} not-whitespace?)
      (find-namespace)
      (extract-namespace-sym))
      ;second)
      ;(z/find up-or-left ns?))
      ;(z/root-string))


  #_(-> loc
        (z/find-last-by-pos {:row 13 :col 5 :end-row 13 :end-col 5} not-whitespace?)
        (find-namespace)))
      ;(z/sexpr)
      ;(second))
      ;(find-namespace)
      ;(extract-namespace-sym))

