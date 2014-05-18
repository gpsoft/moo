(ns moo.core)

(declare init-model create-game)

(load "gui")

(def moo (atom nil))

(def command-table
  {"new" :new
   "help" :help
   "quit" :quit
   "exit" :exit})

(def handler-table
  {:new (fn [_] (create-game))
   :guess #(@moo %)
   :help (fn [_] [:keep :help])
   :quit (fn [_] (@moo :quit))
   :exit (fn [_] nil)
   :unknown (fn [_] [:keep :bad-command])})

(def help-text
  (str
    "Acceptable commands are:" \newline
    "  help  ... Show help message." \newline
    "  new   ... Start a new game." \newline
    "  CODE  ... Your guess like 123." \newline
    "  quit  ... Quit the game." \newline
    "  exit  ... Exit the program."))

(def available-commands
  {:pre-game #{:new :help :exit :unknown}
   :in-game #{:guess :help :quit :exit :unknown}})

(defn match-mark
  [guess code]
  (let [count-true
        (comp count (partial filter identity))
        h (count-true (map = guess code))
        e (count-true (for [g guess c code]
                        (= g c)))]
    (str h \H (- e h) \E)))

(defn moo-fn
  [code]
  (fn [arg]  ; arg should be :quit or a guess.
    (if (= arg :quit)
      [:pre-game :lose code]
      (if (= arg code)
        [:pre-game :win code]
        [:keep-in-game arg
         (match-mark arg code)]))))

(defn gen-code []
  (vec (take 3 (shuffle (range 1 10)))))

(defn command-fits-state?
  [cmd state]
  (contains? (available-commands state) cmd))

(defn calc-state []
  (if @moo :in-game :pre-game))

(defn create-game
  []
  (let [code (gen-code)]
    (reset! moo (moo-fn code))
    [:in-game code]))

(defn result-text
  [op [p1 p2]]
  (let [code-str (fn [[a b c]] (str a b c))]
    (case op
      :in-game (str "Good luck."
#_                    p1)       ; for debug.
      :keep-in-game (str (code-str p1) " ... " p2)
      :pre-game
      (if (= p1 :win)
        "That's right, congratulations!"
        (str "Boo! It was " (code-str p2) "."))
      :keep
      (case p1
        :help help-text
        :bad-state "You can't use the command now."
        :bad-command
        "No such command. Type 'help' for usage."
        ""))))

(defn print-result
  [[op & params :as res]]
  (when res
    (println (result-text op params)))
  res)

(defn handle-command
  [[cmd param]]
  (if (command-fits-state? cmd (calc-state))
    (let [[op :as res]
          ((handler-table cmd) param)]
      (when (= op :pre-game) (init-model))
      res)
    [:keep :bad-state]))

(defn command-from-line
  [line]
  (if (nil? line)
    [:exit]
    (if-let
      [[_ _ code]
       (re-find #"^(guess )? *(\d{3})$" line)]
      [:guess (mapv (comp read-string str)
                    (seq code))]
      [(command-table line :unknown)])))

(defn read-command []
  (command-from-line (read-line)))

(defn run-shell []
  (->> (repeatedly read-command)
       (map (comp print-result handle-command))
       (some nil?)))

(defn init-view []
  (println "Welcome to Moo!")
  (println "Type 'help' to see how to play."))

(defn init-model []
  (reset! moo nil))

(defn -main
  [& [opt]]
  (let [ui (or (keyword opt) :cui)]
    (when (#{:cui :gui} ui)
      (init-model)
      (if (= ui :cui)
        (do
          (init-view)
          (run-shell))
        (init-gui-view)))))


