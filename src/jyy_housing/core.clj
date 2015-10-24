(ns jyy-housing.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

(use 'clojure.pprint)

(def ^:dynamic *base-url* "http://jyy.fi/opiskelijalle/asuminen-ja-toimeentulo/asunnot/vapaat-asunnot/")

(def ^:dynamic *title-selector* [:h2])
(def ^:dynamic *content-selector* [:p.content])
(def ^:dynamic *details-selector* [[:p.event-details (html/but :.published)]])
(def ^:dynamic *date-selector* [:p.published :strong])

(defn get-content [elem]
  (first (:content elem)))

(defn parse-title [raw-title]
  (clojure.string/join ""
    (filter string? (:content raw-title))))

(defn parse-content [raw-content]
  (get-content raw-content))

(defn parse-details [raw-details]
  (let [aparment-details (filter string? (:content (first raw-details)))
        landlord-details (filter string? (:content (last raw-details)))]
    (zipmap
     [:size :rent :location :name :email :phone]
     (map
      #(clojure.string/join "" (drop 1 %))
      (concat aparment-details landlord-details)))))

(defn parse-date [raw-date]
  (clojure.string/replace (get-content raw-date) "\n" " "))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn select-jyy-apartment-ads []
  (html/select (fetch-url *base-url*) [:#apartment-r-]))

(defn get-ad-data [ad]
  (let [title (parse-title (first (html/select [ad] *title-selector*)))
        content (parse-content (first (html/select [ad] *content-selector*)))
        details (parse-details (html/select [ad] *details-selector*))
        date (parse-date (first (html/select [ad] *date-selector*)))]
    (zipmap
     [:title :content :details :date]
     [title content details date])))



(defn -main
  [& args]
  (let [ad-list (select-jyy-apartment-ads)]
    (pprint (get-ad-data (first ad-list)))))


