;; copyright (c) 2019-2023 Sean Corfield, all rights reserved

(ns usermanager.controllers.user
  "The main controller for the user management portion of this app."
  (:require [ring.util.response :as resp]
            [selmer.parser :as tmpl]
            [usermanager.model.user-manager :as model]))

(def ^:private changes
  "Count the number of changes (since the last reload)."
  (atom 0))

(defn render-page
  "Each handler function here adds :application/view to the request
  data to indicate which view file they want displayed. This allows
  us to put the rendering logic in one place instead of repeating it
  for every handler."
  [req]
  (let [data (assoc (:params req) :changes @changes)
        view (:application/view req "default")
        html (tmpl/render-file (str "views/user/" view ".html") data)]
    (-> (resp/response (tmpl/render-file "layouts/default.html"
                                         (assoc data :body [:safe html])))
        (resp/content-type "text/html"))))

(defn reset-changes
  [req]
  (reset! changes 0)
  (assoc-in req [:params :message] "The change tracker has been reset."))

(defn default
  [req]
  (assoc-in req [:params :message]
                (str "Welcome to the User Manager application demo! "
                     "This uses just Compojure, Ring, and Selmer.")))

(defn delete-by-id
  [req]
  (swap! changes inc)
  (model/delete-user-by-id (-> req :application/component :database)
                           (get-in req [:params :id]))
  (resp/redirect "/user/list"))

(defn edit
  "Display the add/edit form.

  If the :id parameter is present, we can use it to populate the edit
  form by loading that user's data from the addressbook."
  [req]
  (let [db   (-> req :application/component :database)
        user (when-let [id (not-empty (get-in req [:params :id]))]
               (model/get-user-by-id db id))]
    (-> req
        (update :params assoc
                :user user
                :departments (model/get-departments db))
        (assoc :application/view "form"))))

(defn get-users
  "Render the list view with all the users in the addressbook."
  [req]
  (let [users (model/get-users (-> req :application/component :database))]
    (-> req
        (assoc-in [:params :users] users)
        (assoc :application/view "list"))))

(defn save
  "This works for saving new users as well as updating existing users, by
   delegating to the model, and either passing nil for :xt$id or the value
   that was passed to the edit form."
  [req]
  (swap! changes inc)
  (-> req
      :params
      ;; get just the form fields we care about:
      (select-keys [:id :first_name :last_name :email :department_id])
      ;; convert form fields to numeric:)
      (update :department_id #(some-> % not-empty Long/parseLong))
      (->> (model/save-user (-> req :application/component :database))))
  (resp/redirect "/user/list"))
