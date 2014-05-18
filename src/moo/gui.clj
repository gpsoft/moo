(in-ns 'moo.core)

(use '[clojure.string :only (replace-first)])

(declare handle-command command-from-line result-text)

(import
  (javax.swing JFrame JLabel JPanel JButton JTextArea
               JScrollPane BoxLayout SwingConstants)
  (javax.swing.border EmptyBorder)
  (java.awt.event ActionListener)
  (java.awt GridLayout Dimension Component))

(defn show-result
  [w [op & params :as res]]
  (when op
    (when (= op :in-game) (.setText w ""))
    (when (not= res [:keep :bad-command])
      (.setText w (str (.getText w)
                       (result-text op params)
                       \newline)))))

(defn enter-digit! [l b]
  (let [t (.getText l)
        d (.getText b)]
    (.setText l (replace-first t "_" d))))

(defn invoke-command
  [w s]
  (->> s
       command-from-line
       handle-command
       (show-result w)))

(defn add-button-listener [b f]
  (.addActionListener b
    (reify ActionListener
      (actionPerformed [_ _] (f)))))

(defn setup-listeners
  [new quit clear submit nums his guess]
  (letfn [(clear-guess [] (.setText guess "___"))]
    (add-button-listener new
      #(invoke-command his "new"))
    (add-button-listener quit
      #(invoke-command his "quit"))
    (add-button-listener clear
      #(clear-guess))
    (add-button-listener submit
      (fn []
        (invoke-command his (.getText guess))
        (clear-guess)))
    (dorun
      (map
        (fn [b] (add-button-listener
                  b #(enter-digit! guess b)))
        nums))))

(defn show-frame []
  (let [frame (JFrame. "Moo")
        new (JButton. "New")
        quit (JButton. "Quit")
        his (JTextArea. "")
        guess-label (JLabel. "Guess:")
        guess (JLabel. "___")
        clear (JButton. "Clear")
        submit (JButton. "Guess")
        nums (doall (map #(JButton. (str %))
                         (range 1 10)))]

    (setup-listeners new quit clear submit nums
                     his guess)

    ;; Setup Layouts.
    (let [pleft (JPanel.)
          pright (JPanel.)
          w 400
          h 250
          bh 30
          eb (EmptyBorder. 5 5 5 5)]

      ;; Left half.
      (let [p (JPanel.)]
        (doto p
          (.setLayout (GridLayout. 1 2))
          (.setMaximumSize
            (Dimension. Short/MAX_VALUE bh))
          (.add new)
          (.add quit)
          (.setAlignmentX Component/CENTER_ALIGNMENT))
        (.setBorder his eb)
        (doto pleft
          (.setLayout (BoxLayout. pleft
                                  BoxLayout/Y_AXIS))
          (.add p)
          (.add (JScrollPane. his))))

      ;; Right half.
      ;; ...which has three sub panels.
      (let [top (JPanel.)
            mid (JPanel.)
            bot (JPanel.)]

        ;; Top sub panel.
        (.setHorizontalAlignment guess-label
                                 SwingConstants/RIGHT)
        (.setBorder guess-label eb)
        (doto top
          (.setLayout (GridLayout. 1 2))
          (.setMaximumSize
            (Dimension. Short/MAX_VALUE bh))
          (.add guess-label)
          (.add guess))

        ;; Middle sub panel.
        (.setLayout mid (GridLayout. 3 3))
        (dorun (map #(.add mid %) nums))

        ;; Bottom sub panel.
        (doto bot
          (.setLayout (GridLayout. 1 2))
          (.setMaximumSize
            (Dimension. Short/MAX_VALUE bh))
          (.add clear)
          (.add submit))

        ;; All sub panels into pright.
        (doto pright
          (.setLayout (BoxLayout. pright
                                  BoxLayout/Y_AXIS))
          (.add top)
          (.add mid)
          (.add bot)))

      ;; Join all.
      (doto frame
        (.setLayout (GridLayout. 1 2))
        (.add pleft)
        (.add pright)
        (.setDefaultCloseOperation
          javax.swing.WindowConstants/EXIT_ON_CLOSE)
        (.setSize w h)
        (.setResizable false)
        (.setVisible true)))
    ))

(defn init-gui-view
  []
  (show-frame))


