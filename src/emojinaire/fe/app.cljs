(ns emojinaire.fe.app
  (:require [cljs.core.async :refer [<! timeout]]
            [clojure.walk :as walk]
            [datascript.core :as ds]
            [emojinaire.fe.emoji :as emoji]
            [emojinaire.fe.home :as home]
            [emojinaire.fe.page-prepare :as prep]
            [replicant.dom :as d])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare actions-handler)

(def schema {:grid/idx {:db/unique :db.unique/identity}})

(def data (emoji/new-board))

(defn transact! [conn data]
  (ds/transact! conn (conj data {:db/id "datomic.tx" :db/txInstant (js/Date.)})))

(defonce conn
  (let [conn (ds/create-conn schema)]
    (transact! conn data)
    conn))

(defn execute-transaction
  [conn tx-data]
  (apply transact! conn tx-data))

(defn execute-transition
  [ms actions]
  (go
    (doseq [action actions]
      (actions-handler nil [action])
      (<! (timeout ms)))))

(defn execute-actions
  [conn actions]
  (doseq [[action & args] actions]
    (apply prn 'Execute action args)
    (case action
      :action/transact (execute-transaction conn args)
      :action/transition (execute-transition (first args) (second args)))))

(defn actions-handler
  [metadata actions]
  (let [e (:replicant/js-event metadata)]
    (->> actions
         (walk/postwalk
          (fn [x]
            (cond
              (= :event/target.value x)
              (some-> e .-target .-value)
              
              (= :event/target.checked x)
              (some-> e .-target .-checked)
              
              :else x)))
         (execute-actions conn))))

(defn- app [db]
  (let [page-data (cond-> db prep/home prep/home)
        ds-data (->> (ds/datoms db :eavt)
                    (map :e)
                    (distinct)
                    (into [])
                    (map #(->> %
                              (ds/entity db)
                              (into {})))
                    (filter #(nil? (:db/txInstant %))))]
    (js/console.log "[STATE DATA]" (clj->js ds-data))
    (home/page page-data)))

(defn ^:dev/after-load start []
  (js/console.log "[START]")
  (d/set-dispatch! actions-handler)
  (add-watch conn
             :app
             (fn [_ _ _ _]
               (d/render (js/document.getElementById "app")
                         (app (ds/db conn)))))
  (transact! conn [{:initialized true :db/ident :initialized}]))

(defn init []
  (js/console.log "[INIT]")
  (start))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^:dev/before-load stop []
  (js/console.log "[STOP]"))

(comment

  (def db (ds/db conn))
  ; Retrieve all of datascript fields [1]
  (->> (ds/q '[:find (pull ?e [*])
               :where
               [?e]
               (not [?e :db/txInstant])]
             db)
       (flatten))
  
  ; Retrieve all of datascript fields [2]
  (->> (ds/datoms db :eavt)
       (map :e)
       (distinct)
       (into [])
       (map #(ds/entity db %))
       (map #(into {} %))
       (filter #(nil? (:db/txInstant %))))

  (ds/q '[:find ?board .
          :where [_ :board ?board]]
        db)

  (->> db
       (ds/q '[:find [?e ...]
               :where [?e :grid/idx]])
       (map #(into {} (ds/entity db %))))

  :rfc
  )
