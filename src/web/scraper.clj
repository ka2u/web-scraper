(ns web.scraper
  (:require [clojurewerkz.urly.core :as urly]
            [net.cgrand.enlive-html :as enlive]
            [clojure.pprint :as pp]
            [clojure.string :as string])
  (:import [com.gargoylesoftware.htmlunit.util NameValuePair]
           [com.gargoylesoftware.htmlunit WebClient BrowserVersion WebRequest HttpMethod]
           [java.io StringReader]
           [java.net URL]
           [java.util ArrayList]))
; browsers
(def browser-chrome (BrowserVersion/CHROME))
(def browser-firfox (BrowserVersion/FIREFOX_24))
(def browser-ie (BrowserVersion/INTERNET_EXPLORER_11))

(defn make-browser 
  "Makes your browser."
  [app-name app-version useragent version-numeric]
  (BrowserVersion. app-name app-version useragent version-numeric))

(defmacro ^:private defset-option [what]
  (let [method-name (symbol (str "set-" (name what)))
        dispatch (symbol (apply str "set" (map string/capitalize (string/split (name what) #"-"))))]
    `(defn ~method-name [client# flag#]
       (.. client# getOptions (~dispatch flag#)))))

(defset-option active-x-native)
(defset-option applet-enabled)
(defset-option css-enabled)
(defset-option do-not-track-enabled)
(defset-option geolocation-enabled)
(defset-option popup-blocker-enabled)
(defset-option print-content-on-failing-status-code)
(defset-option redirect-enabled)
(defset-option throw-exception-on-failing-status-code)
(defset-option throw-exception-on-script-error)

(defn set-javascript-enabled 
  "Sets enable JavaScript."
  [client flag]
  (.. client getOptions (setJavaScriptEnabled flag)))

(defn set-cookies-enabled 
  "Sets enable cookies"
  [client flag]
  (.. client getCookieManager (setCookiesEnabled flag)))

(defn default-mode 
  "Takes the client and sets common browser options."
  [client]
  (set-throw-exception-on-script-error client false)
  (set-redirect-enabled client true)
  (set-javascript-enabled client true)
  (set-cookies-enabled client true)
  client)

(defn make-client 
  "Takes browser and makes client."
  [option]
  (WebClient. option))

(defn- hash-to-namevaluepair-array 
  "Takes parameters and make NameValuePair ArrayList."
  [param]
  (ArrayList. (map #(NameValuePair. (name (first %))
                                              (str (second %))) param)))
(defn get-page 
  "Takes the client, a url and gets a page."
  [client url]
  (.getPage client url))

(defn post-page 
  "Takes the client, a url, a parameteras and posts a request."
  [client url param-hash]
  (let [req (WebRequest. (java.net.URL. url) (HttpMethod/POST))
        param (hash-to-namevaluepair-array param-hash)]
    (.setRequestParameters req param)
    (.getPage client req)))

(defmacro ^:private defget-element [what]
  (let [method-name (symbol (str "get-" (name what)))
        dispatch (symbol (apply str ".get" (map string/capitalize (string/split (name what) #"-"))))]
    `(defn ~method-name [page# element#]
       (~dispatch page# element#))))

(defget-element form-by-name)
(defget-element element-by-id)
(defget-element element-by-name)
(defget-element element-by-id-and-or-name)
(defget-element elements-by-name)
(defget-element elements-by-tag-name)
(defget-element frame-by-name)
(defget-element input-by-name)

(defn get-forms [page]
  (.getForms page))

(defmacro ^:private defget-info [how]
  (let [method-name (symbol (str "get-" (name how)))
        dispatch (symbol (apply str ".get" 
                         (map string/capitalize (string/split (name how) #"-"))))]
  `(defn ~method-name [page#]
     (~dispatch page#))))

(defget-info url)
(defget-info title-text)
(defget-info page-encoding)
(defget-info web-response)
(defget-info href-attribute)

(defn get-status 
  "Gets a HTTP status code"
  [page]
  (.getStatusCode (get-web-response page)))

(defn input-form 
  "Inputs a form."
  [form input]
  (.setValueAttribute form input))

(defn click 
  "Clicks a button."
  [page]
  (.click page))

(defn page->text 
  "Gets a text by the page."
  [page]
  (.asXml page))

(defn page->enlive 
  "Gets enlived page by the page"
  [page]
  (enlive/html-resource (StringReader. (page->text page))))

(defn select-text-node
  "Selects node by enlive selectors"
  [resource path]
  (enlive/select resource (conj path enlive/text-node)))
