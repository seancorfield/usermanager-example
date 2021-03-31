;; copyright (c) 2019-2021 Sean Corfield, all rights reserved

(ns usermanager.web.controllers.user
  "The main controller for the user management portion of this app."
  (:require [ring.util.response :as resp]
            [selmer.parser :as tmpl]
            [usermanager.department.interface :as department]
            [usermanager.user.interface :as user]))

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
    (-> (resp/response (tmpl/render-file (str "layouts/default.html")
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
                     "This uses just Polylith, Compojure, Ring, and Selmer.")))

(defn delete-by-id
  "Compojure has already coerced the :id parameter to an int."
  [req]
  (swap! changes inc)
  (user/delete-by-id (-> req :application/component :database)
                     (get-in req [:params :id]))
  (resp/redirect "/user/list"))

(defn edit
  "Display the add/edit form.

  If the :id parameter is present, Compojure will have coerced it to an
  int and we can use it to populate the edit form by loading that user's
  data from the addressbook."
  [req]
  (let [db   (-> req :application/component :database)
        user (when-let [id (get-in req [:params :id])]
               (user/get-by-id db id))]
    (-> req
        (update :params assoc
                :user user
                :departments (department/get-departments db))
        (assoc :application/view "form"))))

(defn get-users
  "Render the list view with all the users in the addressbook."
  [req]
  (let [users (user/get-users (-> req :application/component :database))]
    (-> req
        (assoc-in [:params :users] users)
        (assoc :application/view "list"))))

(defn save
  "This works for saving new users as well as updating existing users, by
  delegatin to the model, and either passing nil for :addressbook/id or
  the numeric value that was passed to the edit form."
  [req]
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
           (persistent!)
           (user/save (-> req :application/component :database))))
  (resp/redirect "/user/list"))
