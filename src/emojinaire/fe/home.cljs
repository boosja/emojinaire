(ns emojinaire.fe.home)

(defn page [{:keys [grid btn other reset]}]
  (prn (clj->js (map :emoji grid)))
  [:main
   {:style {:display "grid"
            :gap "2rem"
            :justify-items "center"}}
   [:h1 "Emojinaire"]
   [:div {:style {:display "flex"
                  :gap "1rem"}}
    [:btn {:on {:click btn}} "transition"]
    [:btn {:on {:click other}} "other"]
    [:btn {:on {:click reset}} "reset"]]
   [:div.grid
    {:style {:display "grid"
             :grid-template-columns "repeat(10, 50px)"
             :grid-template-rows "repeat(10, 50px)"
             :align-items "center"
             :justify-items "center"
             :width "500px"
             :aspect-ratio "1/1"
             :user-select "none"}}
    (map (fn [{:keys [emoji action]}]
           [:div
            {:style {:width "100%"
                     :height "100%"
                     :display "grid"
                     :align-items "center"
                     :justify-items "center"
                     :font-size "3rem"
                     :cursor "pointer"}
             :on {:click action}}
            emoji])
         grid)
    #_(map (fn [row]
             [:div
              (map (fn [cell]
                     [:span "👏"])
                   row)])
           grid)]])
