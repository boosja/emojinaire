(ns emojinaire.fe.page-prepare
  (:require [datascript.core :as ds]
            [emojinaire.fe.emoji :as emoji]))

(defn get-board [db]
  (->> db
       (ds/q '[:find [?e ...]
               :where [?e :grid/idx]])
       (map #(into {} (ds/entity db %)))
       (sort-by :grid/idx)
       (vec)))

(defn get-diagonals-nw-se [size]
  (let [max-sum (* 2 (dec size))]
    (map (fn [k]
           (let [i-range (range (max 0 (- k (dec size))) (inc (min k (dec size))))]
             (map (fn [i]
                    (let [j (- k i)]
                      (+ (* i size) j)))
                  i-range)))
         (range (inc max-sum)))))

(defn get-diagonals-ne-sw [size]
  (let [min-diff (- (dec size))
        max-diff (dec size)]
    (map (fn [k]
           (let [i-range (range (max 0 k) (inc (min (dec size) (+ k (dec size)))))]
             (map (fn [i]
                    (let [j (- i k)]
                      (+ (* i size) j)))
                  i-range)))
         (range min-diff (inc max-diff)))))

(defn home [db]
  (let [board (get-board db)]
    (js/console.log "board" (clj->js board))
    {:grid (map (fn [cell]
                  (assoc
                   {:action [[:action/transact
                              [{:grid/idx (:grid/idx cell)
                                :emoji/type :emoji/no-mouth}]]]}
                   :emoji (emoji/->emoji (:emoji/type cell))
                   :idx (:grid/idx cell)))
                board)
     :btn [[:action/transition
            50
            (vec (map (fn [ds]
                        [:action/transact
                         (vec
                          (map (fn [idx]
                                 {:grid/idx idx
                                  :emoji/type :emoji/you})
                               ds))])
                      (get-diagonals-nw-se 10)))]]
     :other [[:action/transition
              50
              (vec (map (fn [ds]
                          [:action/transact
                           (vec
                            (map (fn [idx]
                                   {:grid/idx idx
                                    :emoji/type :emoji/no-mouth})
                                 ds))])
                        (get-diagonals-ne-sw 10)))]]
     :reset [[:action/transact (emoji/new-board)]]}))

(comment

  (get-diagonals-nw-se 10)
  (get-diagonals-ne-sw 10)

  (apply assoc [{:emoji/type :emoji/cloudy
                 :grid/idx 0}
                {:emoji/type :emoji/cloudy
                 :grid/idx 1}
                {:emoji/type :emoji/cloudy
                 :grid/idx 2}
                {:emoji/type :emoji/cloudy
                 :grid/idx 3}
                {:emoji/type :emoji/cloudy
                 :grid/idx 4}
                {:emoji/type :emoji/cloudy
                 :grid/idx 5}
                {:emoji/type :emoji/cloudy
                 :grid/idx 6}
                {:emoji/type :emoji/cloudy
                 :grid/idx 7}
                {:emoji/type :emoji/cloudy
                 :grid/idx 8}]
         (mapcat (fn [idx]
                   [idx {:emoji/type :emoji/clap
                         :grid/idx idx}])
                 (nth (get-diagonals-nw-se 3) 1)))

  :rfc)
