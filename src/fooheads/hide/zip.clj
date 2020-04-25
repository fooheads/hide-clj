(ns fooheads.hide.zip
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.zip.findz :as findz]))  
  

;; 
;; Predicates
;; 

(defn not-whitespace? [zloc]
  "Returns true if `zloc` is not a whitespace node."
  (not= (z/tag zloc) :whitespace))

(defn ns? [zloc]
  "Returns `true` if zloc is positioned on a list node where
  the first value is the symbol 'ns."
  (and (= (z/tag zloc) :list)
       (= (-> zloc z/sexpr first) 'ns)))

(defn sym? [zloc]
  "Returns `true` if zloc is positioned on a token node where
  the value is a symbol"
  (and (= (z/tag zloc) :token)
       (symbol? (-> (z/node zloc) :value))))

;; 
;; Utilities
;;

(defn extract-namespace-sym
  "Extracts the namespace symbol from a zloc positioned
  on the namespace form."
  [zloc]
  (some-> zloc
      (z/down)
      (z/find-next sym?)
      (z/node)
      :value))


;; 
;; Navigation functions
;;

(defn up-or-left 
  "Goes up if it's not at the top level, otherwise goes left."
  [zloc]
  (if (-> zloc z/up second :pnodes)
    (z/up zloc)
    (z/left zloc)))

(defn find-namespace 
  "Finds the namespace node from the `zloc`. 
  Currently only seems to find the top-level namespace.
  Returns nil if no namespace can be found."
  [zloc]
  (z/find zloc up-or-left ns?))

;;
;; Entry point
;; 

(defn zloc [code row col]
  "Creates a zipper from the code and positions zloc at
  a position in the tree that matches (`row` `col`)."
  (let [pos {:row row :col col :end-row row :end-col col}
        zipper (z/of-string code)]
    (findz/find-last-by-pos zipper pos not-whitespace?)))

