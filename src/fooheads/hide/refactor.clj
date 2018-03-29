(ns fooheads.hide.refactor
  (:require [clj-refactor.edit :as edit]
            [clj-refactor.transform :as r]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.findz :as findz]
            ))

(defn swap-position!
  [zloc cursor-ref offset]
  (swap! cursor-ref edit/read-position zloc offset)
  zloc)

(defn introduce-let 
  "Introduces let around the form under cursor, and binds the 
  form  `binding-name`" 
  [code row col binding-name]

  (let [pos {:row row :col col :end-row row :end-col col}
        new-cursor (atom [row col])

        zloc (-> code
                 (z/of-string)
                 (findz/find-last-by-pos pos #(not= (z/tag %) :whitespace)))

        zpos (meta (z/node zloc))
        offset (- col (:col zpos))]

    (-> zloc
        (edit/mark-position :new-cursor)

        ;; TODO should check if anything has changed
        ;; - should return nil if transformer returned nil
        ;(transformer args)
        (r/introduce-let [binding-name])

        (edit/mark-position :reformat)
        (edit/format-marked)
        (edit/find-mark :new-cursor)
        (swap-position! new-cursor offset)

        (z/root-string))))

(defn move-to-let 
  "Moves the form under cursor the the first let up the thee,
  binding it to `binding-name`" 
  [code row col binding-name]

  (let [pos {:row row :col col :end-row row :end-col col}
        new-cursor (atom [row col])

        zloc (-> code
                 (z/of-string)
                 (findz/find-last-by-pos pos #(not= (z/tag %) :whitespace)))

        zpos (meta (z/node zloc))
        offset (- col (:col zpos))]

    (-> zloc
        (edit/mark-position :new-cursor)

        ;; TODO should check if anything has changed
        ;; - should return nil if transformer returned nil
        ;(transformer args)
        (r/move-to-let [binding-name])
        (z/up)

        (edit/mark-position :reformat)
        (edit/format-marked)
        (edit/find-mark :new-cursor)
        (swap-position! new-cursor offset)

        (z/root-string))))
