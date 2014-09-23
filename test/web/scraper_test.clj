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
  (GET "/tagname" [] "<a href=\"/foo/bar\">contents</a>")
  (GET "/input" [] "<form name=\"test\"><input name=\"check\" type=\"checkbox\"><submit></form>")
  (GET "/node" [] "<div><p>hello world<p></div>")
  (POST "/post" [] "hello world"))

(use-fixtures :once
  (fn [f]
    (let [server (run-server (compojure.handler/site test-routes) {:port 4347})]
      (try (f) (finally (server))))))

(def client (default-mode (make-client browser-chrome)))

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

(deftest test-get-elements-by-tag-name
  (is (= "class com.gargoylesoftware.htmlunit.html.XPathDomNodeList"
         (-> (get-page client "http://127.0.0.1:4347/tagname")
             (get-elements-by-tag-name "a")
             (class)
             (str)))))

(deftest test-input-by-name
  (is (= "class com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput"
         (-> (get-page client "http://127.0.0.1:4347/input")
             (get-form-by-name "test")
             (get-input-by-name "check")
             (class)
             (str)))))

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
