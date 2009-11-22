; Greedy coin selector

;; ============================================================
(defstruct coin :name :value)
(defn create-coins [col] (map #(apply (partial struct coin) %) col))
(def us-coins (create-coins {'Quarter 25, 'Dime 10, 'Nickel 5, 'Penny 1}))
(defn plural [word n]
   (if (= n 1)
     word
     (let [pos (dec (count word))]
       (if (= (subs word pos) "y")
         (str (subs word 0 pos) "ies")
         (str word "s")))))

; Destructering version
(defn change [amount [{name :name, value :value} & coins]]
  (if (nil? name)
    []
    (let [count (quot amount value)]
      (cons [count (plural (str name) count)]
            (change (rem amount value) coins)))))

(println (change 49 us-coins))

;; ============================================================

(defn reduce-test [amount l]
  (reduce
   (fn [amount coin]
     (let [name (str (:name coin))
           value (:value coin)
           count (quot amount value)]
       (println (str count " " (plural name count)))
       (- amount (* count value))))
   amount l))
         
;(println (reduce-test 49 us-coins))
