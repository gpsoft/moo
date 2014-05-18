(ns moo.core-test
  (:use
    moo.core
    midje.sweet))

(load "gui_test")

(tabular "about match-mark"
    (fact (match-mark ?g ?c) => ?m)
    ?g      ?c      ?m
    [1 2 3] [1 2 3] "3H0E"
    [1 2 4] [1 2 3] "2H0E"
    [1 4 3] [1 2 3] "2H0E"
    [4 2 3] [1 2 3] "2H0E"
    [1 3 2] [1 2 3] "1H2E"
    [3 2 1] [1 2 3] "1H2E"
    [2 1 3] [1 2 3] "1H2E"
    [2 3 1] [1 2 3] "0H3E"
    [3 1 2] [1 2 3] "0H3E"
    [4 1 2] [1 2 3] "0H2E"
    [2 4 1] [1 2 3] "0H2E"
    [2 1 4] [1 2 3] "0H2E"
    [4 1 5] [1 2 3] "0H1E"
    [4 5 1] [1 2 3] "0H1E"
    [2 4 5] [1 2 3] "0H1E"
    [4 5 2] [1 2 3] "0H1E"
    [3 4 5] [1 2 3] "0H1E"
    [4 3 5] [1 2 3] "0H1E"
    [4 5 6] [1 2 3] "0H0E"
    [1 1 1] [1 2 3] "1H2E"
    [2 2 2] [1 2 3] "1H2E"
    [3 3 3] [1 2 3] "1H2E"
    [4 4 4] [1 2 3] "0H0E"
    )

(fact "about moo-fn"
  (moo-fn [7 2 6]) => fn?
  ((moo-fn [7 2 6]) :quit) =>
  [:pre-game :lose [7 2 6]]
  ((moo-fn [7 2 6]) [7 2 6]) =>
  [:pre-game :win [7 2 6]]
  ((moo-fn [7 2 6]) [1 2 3]) =>
  [:keep-in-game [1 2 3] ..MARK..]
  (provided
    (match-mark [1 2 3] [7 2 6]) => ..MARK..))

(fact "about gen-code"
  (dotimes [i 100]
    (gen-code) =>
    (every-checker
      (fn [code]
        (every? #(and (<= 1 %) (<= % 9)) code))
      (fn [code]
        (= (count (distinct code)) 3)))))

(tabular "about command-fits-state?"
  (fact (command-fits-state? ?cmd ?st) => ?fits)
  ?cmd     ?st       ?fits
  :new     :pre-game true
  :guess   :pre-game false
  :quit    :pre-game false
  :help    :pre-game true
  :exit    :pre-game true
  :unknown :pre-game true
  :new     :in-game false
  :guess   :in-game true
  :quit    :in-game true
  :help    :in-game true
  :exit    :in-game true
  :unknown :in-game true)

(fact "about calc-state"
  (reset! moo nil)
  (calc-state) => :pre-game
  (reset! moo 123)
  (calc-state) => :in-game)

(fact "about create-game"
  (reset! moo nil)
  (create-game) => [:in-game ..CODE..]
  (provided
    (gen-code) => ..CODE..
    (moo-fn ..CODE..) => ..MOO..)
  @moo => ..MOO..)

(tabular "about result-test"
  (fact (result-text ?op ?params) => ?text)
  ?op            ?params          ?text
  :in-game       [[7 2 6]]        "Good luck."
  :keep-in-game  [[1 2 3] "1H0E"] "123 ... 1H0E"
  :pre-game      [:win [7 2 6]]   "That's right, congratulations!"
  :pre-game      [:lose [7 2 6]]  "Boo! It was 726."
  :keep          [:help]         
  (str
    "Acceptable commands are:" \newline
    "  help  ... Show help message." \newline
    "  new   ... Start a new game." \newline
    "  CODE  ... Your guess like 123." \newline
    "  quit  ... Quit the game." \newline
    "  exit  ... Exit the program.")
  :keep          [:bad-state]     "You can't use the command now."
  :keep          [:bad-command]   "No such command. Type 'help' for usage.")

(fact "about print-result"
  (print-result [:a]) => [:a]
  (provided
    (println (result-text :a nil)) => nil)

  (print-result [:a :b]) => [:a :b]
  (provided
    (println (result-text :a '(:b))) => nil)

  (print-result [:a :b :c]) => [:a :b :c]
  (provided
    (println (result-text :a '(:b :c))) => nil)

  (print-result nil) => nil
  (provided
    (result-text ..OP.. ..PARAMS..) => anything :times 0
    (println ..TEXT..) => anything :times 0)
  )

(fact "about handle-command"
  (handle-command [:new]) => [:keep :bad-state]
  (provided
    (command-fits-state? :new (calc-state)) => false)

  (handle-command [:new]) => [:pre-game]
  (provided
    (command-fits-state? :new (calc-state)) => true
    (create-game) => [:pre-game]
    (init-model) => ..ANY..)

  (handle-command [:new]) => [:in-game ..CODE..]
  (provided
    (command-fits-state? :new (calc-state)) => true
    (create-game) => [:in-game ..CODE..])

  (handle-command [:help]) => [:keep :help]
  (provided
    (command-fits-state? :help (calc-state)) => true)

  (handle-command [:exit]) => nil
  (provided
    (command-fits-state? :exit (calc-state)) => true)

  (handle-command [:unknown]) => [:keep :bad-command]
  (provided
    (command-fits-state? :unknown (calc-state)) => true)
  (defn dummy-moo [_])
  (reset! moo #'dummy-moo)

  (handle-command [:guess [1 2 3]]) =>
  [:op ..PARA1.. ..PARA2..]
  (provided
    (command-fits-state? :guess (calc-state)) => true
    (dummy-moo [1 2 3]) => [:op ..PARA1.. ..PARA2..])

  (handle-command [:quit]) => [:op ..PARA1.. ..PARA2..]
  (provided
    (command-fits-state? :quit (calc-state)) => true
    (dummy-moo :quit) => [:op ..PARA1.. ..PARA2..])

  )

(tabular "about command-from-line"
  (fact (command-from-line ?l) => ?c)
  ?l          ?c
  "exit"      [:exit]
  "new"       [:new]
  "quit"      [:quit]
  "guess 123" [:guess [1 2 3]]
  "123"       [:guess [1 2 3]]
  "gue 123"   [:unknown]
  "1234"      [:unknown]
  "foo"       [:unknown]
  nil         [:exit])

(fact "about read-command"
    (read-command) => ..CMD..
    (provided
          (command-from-line (read-line)) => ..CMD..))

(fact "about run-shell"
  (run-shell) => anything
  (provided
    (read-command) => ..CMD.. :times 1
    (handle-command ..CMD..) => nil :times 1
    (print-result nil) => nil :times 1)  ;←

  (run-shell) => anything
  (provided
    (read-command) =streams=>
    [..CMD1.. ..CMD2.. ..CMD3..] :times 3
    (handle-command ..CMD1..) => ..RES1.. :times 1
    (handle-command ..CMD2..) => ..RES2.. :times 1
    (handle-command ..CMD3..) => nil :times 1
    (print-result ..RES1..) => ..RES1.. :times 1  ;←
    (print-result ..RES2..) => ..RES2.. :times 1  ;←
    (print-result nil) => nil :times 1))  ;←

(fact "about init-view"
  (with-out-str
    (init-view)) =>
  #"^Welcome to Moo!(\n|\r\n|\r)Type 'help' to see how to play.(\n|\r\n|\r)$")

(fact "about init-model"
  (reset! moo 123)
  (init-model) => anything
  @moo => nil)

(fact "about -main"
  (-main) => ..C..
  (provided
    (init-model) => ..A.. :times 1
    (init-view) => ..B.. :times 1
    (run-shell) => ..C.. :times 1
    (init-gui-view) => anything :times 0)

  (-main "cui") => ..C..
  (provided
    (init-model) => ..A.. :times 1
    (init-view) => ..B.. :times 1
    (run-shell) => ..C.. :times 1
    (init-gui-view) => anything :times 0)

  (-main "gui") => ..B..
  (provided
    (init-model) => ..A.. :times 1
    (init-view) => anything :times 0
    (run-shell) => anything :times 0
    (init-gui-view) => ..B.. :times 1)

  (-main "foo") => nil
  (provided
    (init-model) => anything :times 0
    (init-view) => anything :times 0
    (run-shell) => anything :times 0
    (init-gui-view) => anything :times 0)
  )

(fact "about handle-command in action"
  (init-model)
  (init-view)

  (handle-command [:new]) => [:in-game [1 2 3]]
  (provided (gen-code) => [1 2 3])

  (handle-command [:guess [2 5 8]]) =>
  [:keep-in-game [2 5 8] "0H1E"]
  (handle-command [:guess [4 9 6]]) =>
  [:keep-in-game [4 9 6] "0H0E"]
  (handle-command [:guess [3 2 7]]) =>
  [:keep-in-game [3 2 7] "1H1E"]
  (handle-command [:help]) => [:keep :help]
  (handle-command [:guess [1 2 3]]) =>
  [:pre-game :win [1 2 3]]
  (handle-command [:exit]) => nil
  )

(fact "about failing handle-command"
  (handle-command [:new]) => (throws RuntimeException)
  (provided
    (gen-code) =throws=> (RuntimeException. "Boom!")))

(fact "about the app"
  (with-out-str
    (with-in-str
      (str "new" \newline
           "258" \newline
           "496" \newline
           "327" \newline
           "help" \newline
           "guess 123" \newline
           "exit" \newline)
      (-main))) =>
  #"(?s)Welcome.+0H1E.+0H0E.+1H1E.+Acceptable commands.+congrat.+"
  (provided (gen-code) => [1 2 3])
  )

