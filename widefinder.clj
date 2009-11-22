;; Example from http://technomancy.us/130
;;

(ns widefinder
  "A basic map/reduce approach to the wide finder using agents.
  Optimized for being idiomatic and readable rather than speed."
  (:use [clojure.contrib.duck-streams :only [reader]])
  (:gen-class))

;(def file-name "/usr/share/dict/words")
(def file-name "words")
(def num-agents 10)
(def top-n 5)
(def re #"^..(...)")
;(def re #"GET /(\d+) ")

(defn count-line [counts line]
  (if-let [[_ hit] (re-find re line)]
    (assoc counts hit (inc (get counts hit 0)))
    counts))

(defn find-widely [filename n]
  (let [agents (map agent (repeat n {}))]
    (dorun (map #(send %1 count-line %2)
                (cycle agents)
                (line-seq (reader filename))))
    (doseq [a agents] (await a))
    (apply merge-with + (map deref agents))))

(defn -main []
  (def result (find-widely file-name num-agents))
  (println (take top-n  (reverse (sort-by val result))))
  (shutdown-agents)
)

(time(-main))
