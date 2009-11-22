(ns my-wide-finder
  "A basic map/reduce approach to the wide finder using agents.
  Optimized for being idiomatic and readable rather than speed.
  NOTE: Originally from:
  http://technomancy.us/130
  but updated to use pmap."
  (:use [clojure.contrib.duck-streams :only [reader]]))


(def file-name "words")
(def re #"GET /(\d+) ")
(def re #"^..(...)")
(def top-n 5)

(defn count-line
  "Increment the relevant entry in the counts map."
  [line]
  (if-let [[_ hit] (re-find re line)]
    {hit 1}
    {}))

(defn my-find-widely
  "Return a map of pages to hit counts in filename."
  [filename]
  (apply merge-with +
         (pmap count-line (line-seq (reader filename)))))

(def result (my-find-widely file-name))
(println (take top-n  (reverse (sort-by val result))))
(shutdown-agents)

