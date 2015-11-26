(ns jyy-housing.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]
            [gmail-clj.core :as gmail]
            [environ.core :as environ]
            [clojure.tools.logging :as log])
  (:use clojure.set))

;; Url for housing ads
(def ^:dynamic *base-url* "http://jyy.fi/opiskelijalle/asuminen-ja-toimeentulo/asunnot/vapaat-asunnot/")

;; Selectors for web scraping
(def ^:dynamic *title-selector* [:h2])
(def ^:dynamic *content-selector* [:p.content])
(def ^:dynamic *details-selector* [[:p.event-details (html/but :.published)]])
(def ^:dynamic *date-selector* [:p.published :strong])

;; Results from last scrape
(def ^:dynamic *last-result* #{1 2 3})

(defn get-content [elem]
  (log/info "get-content")
  (first (:content elem)))

(defn parse-title [raw-title]
  (clojure.string/join ""
    (filter string? (:content raw-title))))

(defn parse-content [raw-content]
  (log/info "parse-content")
  (get-content raw-content))

(defn parse-details [raw-details]
  (log/info "parse-details")
  (let [aparment-details (filter string? (:content (first raw-details)))
        landlord-details (filter string? (:content (last raw-details)))]
    (zipmap
     [:size :rent :location :name :email :phone]
     (map
      #(clojure.string/join "" (drop 1 %))
      (concat aparment-details landlord-details)))))

(defn parse-date [raw-date]
  (log/info "parse-date")
  (log/info raw-date)
  (clojure.string/replace (get-content raw-date) "\n" " "))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn select-jyy-apartment-ads []
  (html/select (fetch-url *base-url*) [:#apartment-r-]))

(defn get-ad-data [ad]
  (println ad)
  (log/info "get-ad-data")
  (let [title (parse-title (first (html/select [ad] *title-selector*)))
        content (parse-content (first (html/select [ad] *content-selector*)))
        details (parse-details (html/select [ad] *details-selector*))
        date (parse-date (first (html/select [ad] *date-selector*)))]
      (zipmap
       [:title :content :details :date]
       [title content details date])))

(defn set-mail-auth-variables []
  (log/info "set-mail-auth-variables")
  (gmail/set-client-secret! (environ/env :mail-client-secret))
  (gmail/set-client-id! (environ/env :mail-client-id))
  (gmail/set-refresh-token! (environ/env :mail-refresh-token)))

(defn concat-details [details]
  (clojure.string/join " " details))

(defn format-mail [ad]
  (log/info "Format mail")
  (log/info ad)
  (clojure.string/join "\n" [(:title ad)
                             (:content ad)
                             (str (concat-details (vals (:details ad))) "\n" (:date ad))]))

(defn get-ad-split-string []
  (str "\n\n" (repeat 20 "*") "\n\n"))

(defn format-mail-seq [ad-seq]
  (log/info "Format mail seq")
  (log/info ad-seq)
  {:to "villej.toiviainen@gmail.com"
   :subject "Clojure test"
   :body (clojure.string/join (get-ad-split-string) (map format-mail ad-seq))})


(defn send-mail [ad-seq]
  (let [mail (format-mail-seq ad-seq)]
    (log/info "Sending mail to")
    (log/info mail)
    (gmail/message-send mail)))

(defn compare-latests-results [latest-result]
  (log/info "compare-latests-results")
  (clojure.set/difference latest-result *last-result*))

(defn set-last-result [result]
  (log/info "set-last-result")
   (def *last-result* result))

(defn handle-scrape-result [ad-list]
  (log/info "handle-scrape-result")
  (let [result (set (map get-ad-data ad-list))]
    (log/info result)
    (if (empty? *last-result*)
      (set-last-result result)
      (send-mail (compare-latests-results result)))))


(defn -main
  [& args]
  (log/info "Starting the app")
  (let [ad-list (select-jyy-apartment-ads)]
    (log/info "Setting mail client env")
    (set-mail-auth-variables)
    (handle-scrape-result ad-list)))



