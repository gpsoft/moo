(defproject moo "1.0.0"
  :description "A game like Bulls and Cows, Hit and Blow, or Code Breaker"
  :url "https://github.com/gpsoft/moo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}}
  :main moo.core
)
