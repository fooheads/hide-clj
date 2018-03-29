(ns fooheads.hide)

(defn eval-code
  "Evaluates the code"
  [code]
  (clojure.core/eval (read-string code)))

