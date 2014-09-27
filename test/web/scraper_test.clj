(ns web.scraper-test
  (:use compojure.core
        [ring.adapter.jetty :only [run-jetty]]
        [org.httpkit.server :only [run-server]])
  (:require [clojure.test :refer :all]
            [web.scraper :refer :all]
            [compojure.route]
            [compojure.handler]
            [clojure.string]
            [clojure.pprint]
            [net.cgrand.enlive-html]))

(defroutes test-routes
  (GET "/get" [] "hello world")
  (GET "/form" [] "<form name=\"test\"><submit></form>")
  (GET "/element-id" [] "<div id=\"test\">contents</div>")
  (GET "/element-name" [] "<div name=\"test\">contents</div>")
  (GET "/tagname" [] "<a href=\"/foo/bar\">contents</a>")
  (GET "/input" [] "<form name=\"test\"><input name=\"check\" type=\"checkbox\"><submit></form>")
  (GET "/node" [] "<div><p>hello world<p></div>")
  (GET "/frame" [] "<iframe name=test></iframe>")
  (GET "/title" [] "<html><head><title>test</title></head></html>")
  (GET "/encoding" [] "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>test</title></head></html>")
  (POST "/post" [] "hello world"))

(use-fixtures :once
  (fn [f]
    (let [server (run-server (compojure.handler/site test-routes) {:port 4347})]
      (try (f) (finally (server))))))


(defmacro ^:private deftest-option-operator [type method value client]
  (let [setter (symbol (str "set-" (name method) "!"))
        test-name (symbol (str "test-" setter))
        is-method (symbol (str (name type) "-" (name method)))
        bool (symbol (str value "?"))]
    `(deftest ~test-name
         (is (~bool (~is-method ~client)))
         (is (true? (do
                      (~setter ~client true)
                      (~is-method ~client)))))))

(let [client (make-client browser-chrome)]
  (deftest-option-operator :is active-x-native false client)
  (deftest-option-operator :is applet-enabled false client)
  (deftest-option-operator :is css-enabled true client)
  (deftest-option-operator :is do-not-track-enabled false client)
  (deftest-option-operator :is geolocation-enabled false client)
  (deftest-option-operator :is popup-blocker-enabled false client)
  (deftest-option-operator :get print-content-on-failing-status-code true client)
  (deftest-option-operator :is redirect-enabled true client)
  (deftest-option-operator :is throw-exception-on-failing-status-code true client)
  (deftest-option-operator :is throw-exception-on-script-error true client)
  (deftest-option-operator :is javascript-enabled true client)
  (deftest-option-operator :is cookies-enabled true client))


(def client (default-mode! (make-client browser-chrome)))
(deftest test-get-page
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlPage"
         (-> (get-page client "http://127.0.0.1:4347/get")
             (class)
             (str)))))

(deftest test-post-page
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlPage"
         (-> (post-page client "http://127.0.0.1:4347/post" {:foo 1, :bar 2})
             (class)
             (str)))))

(deftest test-get-form-by-name
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlForm"
         (-> (get-page client "http://127.0.0.1:4347/form")
             (get-form-by-name "test")
             (class)
             (str)))))

(deftest test-get-element-by-id
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlDivision"
         (-> (get-page client "http://127.0.0.1:4347/element-id")
             (get-element-by-id "test")
             (class)
             (str)))))

(deftest test-get-element-by-name
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlDivision"
         (-> (get-page client "http://127.0.0.1:4347/element-name")
             (get-element-by-name "test")
             (class)
             (str)))))

(deftest test-get-elements-by-id-and-or-name
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlDivision"
         (-> (get-page client "http://127.0.0.1:4347/element-name")
             (get-elements-by-id-and-or-name "test")
             (first)
             (class)
             (str)))))

(deftest test-get-elements-by-name
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlDivision"
         (-> (get-page client "http://127.0.0.1:4347/element-name")
             (get-elements-by-name "test")
             (first)
             (class)
             (str)))))

(deftest test-get-elements-by-tag-name
  (is (= "class com.gargoylesoftware.htmlunit.html.XPathDomNodeList"
         (-> (get-page client "http://127.0.0.1:4347/tagname")
             (get-elements-by-tag-name "a")
             (class)
             (str)))))

(deftest test-get-elements-by-tag-name
  (is (= "class com.gargoylesoftware.htmlunit.html.FrameWindow"
         (-> (get-page client "http://127.0.0.1:4347/frame")
             (get-frame-by-name "test")
             (class)
             (str)))))

(deftest test-input-by-name
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput"
         (-> (get-page client "http://127.0.0.1:4347/input")
             (get-form-by-name "test")
             (get-input-by-name "check")
             (class)
             (str)))))

(deftest test-get-url
  (is (= "http://127.0.0.1:4347/get"
         (-> (get-page client "http://127.0.0.1:4347/get")
             (get-url)
             (str)))))

(deftest test-get-title-text
  (is (= "http://127.0.0.1:4347/get"
         (-> (get-page client "http://127.0.0.1:4347/get")
             (get-url)
             (str)))))

(deftest test-get-title-text
  (is (= "test"
         (-> (get-page client "http://127.0.0.1:4347/title")
             (get-title-text)
             (str)))))

(deftest test-get-page-encoding
  (is (= "UTF-8"
         (-> (get-page client "http://127.0.0.1:4347/encoding")
             (get-page-encoding)
             (str)))))

(deftest test-get-web-response
  (is (= "class com.gargoylesoftware.htmlunit.WebResponse"
         (-> (get-page client "http://127.0.0.1:4347/get")
             (get-web-response)
             (class)
             (str)))))

(deftest test-get-href-attribute
  (is (= "/foo/bar"
         (-> (get-page client "http://127.0.0.1:4347/tagname")
             (as-> x (get-elements-by-tag-name x "a")
                   (first x)
                   (get-href-attribute x)
                   (str x))))))

(deftest test-get-status
  (is (= 200
         (-> (get-page client "http://127.0.0.1:4347/get")
             (get-status)))))

(deftest test-page->text
  (is (= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<html>\r\n  <head/>\r\n  <body>\r\n    hello world\r\n  </body>\r\n</html>\r\n"
         (-> (get-page client "http://127.0.0.1:4347/get")
             (page->text)))))

(deftest test-page->enlive
  (is (= 3
         (-> (get-page client "http://127.0.0.1:4347/get")
             (page->enlive)
             (net.cgrand.enlive-html/select [:*])
             count))))

(deftest test-select-text-node
  (is (= "\n        hello world\n      \n      "
         (apply str 
                (-> (get-page client "http://127.0.0.1:4347/node")
                    (page->enlive)
                    (select-text-node [:div :p]))))))
