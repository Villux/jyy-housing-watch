(defproject jyy-housing "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.7.0"]
                 [enlive "1.1.5"]
                 [gmail-clj "0.6.3"]
                 [environ "1.0.1"]
                 [org.clojure/tools.logging "0.3.1"]]
  :main ^:skip-aot jyy-housing.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
