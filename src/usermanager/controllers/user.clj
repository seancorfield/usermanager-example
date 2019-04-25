;; copyright (c) 2019 Sean Corfield, all rights reserved

(ns usermanager.controllers.user
  (:require [ring.util.response :as resp]
            [selmer.parser :as tmpl]
            [usermanager.model.user-manager :as model]))

(def ^:private changes
  "Count the number of changes (since the last reload)."
  (atom 0))

(defn before [req]
  ;; whatever needs doing at the start of the request
  req)

(defn after [req]
  (if (resp/response? req)
    req
    ;; no response so far, render an HTML template
    (let [data (assoc (:params req) :changes @changes)
          view (:application/view req "default")
          html (tmpl/render-file (str "views/user/" view ".html") data)]
      (-> (resp/response (tmpl/render-file (str "layouts/default.html")
                                           (assoc data :body [:safe html])))
          (resp/content-type "text/html")))))

(defn reset-changes [req]
  (reset! changes 0)
  (assoc-in req [:params :message] "The change tracker has been reset."))

(defn default [req]
  (assoc-in req [:params :message]
                (str "Welcome to the User Manager application demo! "
                     "This uses just Compojure, Ring, and Selmer.")))

(defn delete-by-id [req]
  (swap! changes inc)
  (model/delete-user-by-id (get-in req [:params :id]))
  (resp/redirect "/user/list"))

(defn edit [req]
  (let [user (model/get-user-by-id (get-in req [:params :id]))]
    (-> req
        (update :params assoc
                :user user
                :departments (model/get-departments))
        (assoc :application/view "form"))))

(defn get-users [req]
  (let [users (model/get-users)]
    (-> req
        (assoc-in [:params :users] users)
        (assoc :application/view "list"))))

(defn save [req]
  (swap! changes inc)
  (-> req
      :params
      ;; get just the form fields we care about:
      (select-keys [:id :first_name :last_name :email :department_id])
      ;; convert form fields to numeric:
      (update :id            #(some-> % not-empty Long/parseLong))
      (update :department_id #(some-> % not-empty Long/parseLong))
      ;; qualify their names for domain model:
      (->> (reduce-kv (fn [m k v] (assoc! m (keyword "addressbook" (name k)) v))
                      (transient {}))
           (persistent!))
      (model/save-user))
  (resp/redirect "/user/list"))
