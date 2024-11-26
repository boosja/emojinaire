(ns emojinaire.fe.emoji)

(def emojis {:emoji/clap "👏"
             :emoji/no-mouth "😶"
             :emoji/neutral "😐"
             :emoji/cloudy "😶‍🌫️"
             :emoji/you "🫵"})

(defn new-board
  ([] (new-board 100))
  ([size] (map-indexed (fn [idx _]
                         {:emoji/type :emoji/cloudy
                          :grid/idx idx})
                       (repeat size :cell))))

(defn ->emoji [k]
  (k emojis))
