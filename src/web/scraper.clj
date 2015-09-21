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
(def browser-firfox (BrowserVersion/FIREFOX_38))
(def browser-ie (BrowserVersion/INTERNET_EXPLORER_11))

(defn make-browser 
  "Makes your browser."
  [app-name app-version useragent version-numeric]
  (BrowserVersion. app-name app-version useragent version-numeric))


(defn method-name-and-operator [type what]
  (let [method-name (symbol (str (name type) "-" (if (= type :set)
                                                   (str (name what) "!")
                                                   (name what))))
        dispatch (symbol (apply str "." (name type) (map string/capitalize (string/split (name what) #"-"))))]
    [method-name dispatch]))

(defmacro ^:private defoperator [type what]
  (let [[method-name dispatch] (method-name-and-operator type what)]
  `(defn ~method-name 
     ([page#] (~dispatch page#))
     ([page# element#] (~dispatch page# element#)) )))

(defmacro ^:private defoption-operator [type what]
  (let [[method-name dispatch] (method-name-and-operator type what)]
    `(defn ~method-name 
       ([client#] (~dispatch (.getOptions client#)))
       ([client# flag#] (~dispatch (.getOptions client#) flag#)))))

(defoption-operator :set active-x-native)
(defoption-operator :set applet-enabled)
(defoption-operator :set css-enabled)
(defoption-operator :set do-not-track-enabled)
(defoption-operator :set geolocation-enabled)
(defoption-operator :set popup-blocker-enabled)
(defoption-operator :set print-content-on-failing-status-code)
(defoption-operator :set redirect-enabled)
(defoption-operator :set throw-exception-on-failing-status-code)
(defoption-operator :set throw-exception-on-script-error)

(defoption-operator :is active-x-native)
(defoption-operator :is applet-enabled)
(defoption-operator :is css-enabled)
(defoption-operator :is do-not-track-enabled)
(defoption-operator :is geolocation-enabled)
(defoption-operator :is popup-blocker-enabled)
(defoption-operator :get print-content-on-failing-status-code)
(defoption-operator :is redirect-enabled)
(defoption-operator :is throw-exception-on-failing-status-code)
(defoption-operator :is throw-exception-on-script-error)

(defn set-javascript-enabled!
  "Sets enable JavaScript."
  [client flag]
  (.. client getOptions (setJavaScriptEnabled flag)))

(defn is-javascript-enabled
  "Is enable JavaScript?"
  [client]
  (.. client getOptions (isJavaScriptEnabled)))

(defn set-cookies-enabled!
  "Sets enable cookies"
  [client flag]
  (.. client getCookieManager (setCookiesEnabled flag)))

(defn is-cookies-enabled
  "Is enable cookies?"
  [client]
  (.. client getCookieManager (isCookiesEnabled)))

(defn default-mode!
  "Takes the client and sets common browser options."
  [client]
  (set-throw-exception-on-script-error! client false)
  (set-redirect-enabled! client true)
  (set-javascript-enabled! client true)
  (set-cookies-enabled! client true)
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

(defoperator :get form-by-name)
(defoperator :get element-by-id)
(defoperator :get element-by-name)
(defoperator :get elements-by-id-and-or-name)
(defoperator :get elements-by-name)
(defoperator :get elements-by-tag-name)
(defoperator :get frame-by-name)
(defoperator :get input-by-name)
(defoperator :get forms)

(defoperator :get url)
(defoperator :get title-text)
(defoperator :get page-encoding)
(defoperator :get web-response)
(defoperator :get href-attribute)

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
  [button]
  (.click button))

(defn get-elem-from-nodelist-by-index
  "Get element from XNodeList by index."
  [nodelist index]
  (.get nodelist index))

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
