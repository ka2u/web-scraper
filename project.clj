(defproject org.clojars.ka2u/web-scraper "0.0.4"
  :description "Web scraping library. It has fairly good JavaScript support by HtmlUnit."
  :url "https://github.com/ka2u/web-scraper"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [net.sourceforge.htmlunit/htmlunit "2.18"]
                 [clojurewerkz/urly "1.0.0"]
                 [enlive "1.1.6"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.18"]
                 [ring "1.4.0"]
                 [clj-time "0.11.0"]])
