;; Count instances of strings found in a file, parallelized using agents.
;;
;; See
;; http://www.tbray.org/ongoing/When/200x/2009/11/11/Clojure-References
;;
;; - Ross Thomas <halfacanuck@gmail.com>

(use '[clojure.contrib.duck-streams :only (read-lines)]) 

;(def file-name "/usr/share/dict/words")
(def file-name "words")
(def re #"^..(...)")
(def top-n 5)
(def num-agents 2)

;(defstruct counter :key :value)
(def lines (read-lines file-name))
(def agent-pool (map agent (repeat num-agents {})))

(def agents (cycle agent-pool))
(def work-seq (map vector lines agents))

;(defn extract-string [line] (first (next (re-find re line))))
(defn extract-string [line]  (if (>= (count line) 5) (subs line 2 5)))


;; Find match and update agent's state
(defn count-line [counts line]
  (if-let [hit (extract-string line)]
    (assoc counts hit (inc (get counts hit 0)))
    counts))

;; Send work to agents, wait for them to finish
;(doseq [[line a] work-seq] (send a count-line line))
(doseq [work work-seq] (send (first(rest work)) count-line (first work)))
(doseq [a agent-pool] (await a))

;; Produce final map
(time (def result (apply merge-with + (map deref agent-pool))))
(println (type result))
(println (take top-n (reverse (sort-by val result))))

(shutdown-agents)

