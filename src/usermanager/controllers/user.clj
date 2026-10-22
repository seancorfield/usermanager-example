;; copyright (c) 2019-2026 Sean Corfield, all rights reserved

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
  [{:keys! [params]
    :application/keys [view] :or {view "default"}}]
  (let [data (assoc params :changes @changes)
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
  "Compojure has already coerced the :id parameter to an int."
  [{{:keys! [database]} :application/component
    {:keys! [id]} :params}]
  (swap! changes inc)
  (model/delete-user-by-id database id)
  (resp/redirect "/user/list"))

(defn edit
  "Display the add/edit form.

  If the :id parameter is present, Compojure will have coerced it to an
  int and we can use it to populate the edit form by loading that user's
  data from the addressbook."
  [{{:req! {db :database}} :application/component
    {:keys [id]} :params
    :as req}]
  (-> req
      (update :params assoc
              :user (when id (model/get-user-by-id db id))
              :departments (model/get-departments db))
      (assoc :application/view "form")))

(defn get-users
  "Render the list view with all the users in the addressbook."
  [{{:keys! [database]} :application/component
    :as req}]
  (-> req
      (assoc-in [:params :users] (model/get-users database))
      (assoc :application/view "list")))

(defn save
  "This works for saving new users as well as updating existing users, by
  delegating to the model, and either passing nil for :addressbook/id or
  the numeric value that was passed to the edit form."
  [{{:keys! [database]} :application/component
    ;; from the parameters (:params), require these keys to be present and
    ;; select a map containing just those keys:
    {:keys! [& :id :first_name :last_name :email :department_id]
     :select user} :params}]
  (swap! changes inc)
  (-> user
      ;; convert form fields to numeric (or nil, for :id):
      (update :id            #(some-> % not-empty Long/parseLong))
      (update :department_id #(some-> % not-empty Long/parseLong))
      ;; qualify their names for the domain model:
      (update-keys #(keyword "addressbook" (name %)))
      (->> (model/save-user database)))
  (resp/redirect "/user/list"))
