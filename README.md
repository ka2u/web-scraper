# web-scraper

Web scraping library. It has fairly good JavaScript support by HtmlUnit.

## Usage

Add `[org.clojars.ka2u/web-scraper "0.0.2"]` to your project.clj's `:dependecies` .

```
(def client (make-client browser-chrome))
(-> (get-page client "http://example.com/foo/bar")
  (page->enlive)
  (select-text-node [:div#middle :table :tobdy :tr :td.mtext]))
```

## License

Copyright © 2014 Kazuhiro SHIBUYA

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
