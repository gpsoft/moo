(in-ns 'moo.core-test)

(use 'midje.sweet)

(import
  (javax.swing JLabel JButton JTextArea))

(fact "about init-gui-view"
  (init-gui-view) => ..FRAME..
  (provided
    (show-frame) => ..FRAME..))

(fact "about invoke-command"
  (let [b (JButton. "foo")]
    (invoke-command b "bar") => anything
    (provided
      (show-result
        b
        (handle-command
          (command-from-line "bar"))) => anything)))

(fact "about show-result"
  (let [w (JTextArea. "")]
    (show-result w nil) => anything
    (provided
      (result-text anything anything) =>
      anything :times 0)

    (.setText w "DUMMY")
    (show-result w [:in-game ..CODE..]) => anything
    (provided
      (result-text :in-game [..CODE..]) => "abc")
    (show-result w [:op ..P1.. ..P2..]) => anything
    (provided
      (result-text :op [..P1.. ..P2..]) => "efg")
    (show-result w [:keep :bad-command]) => anything
    (provided
      (result-text anything anything) =>
      anything :times 0)
    (.getText w) =>
    (str "abc" \newline "efg" \newline)
))

(fact "about enter-digit!"
  (let [b (JButton. "3")
        l (JLabel. "___")]
    (enter-digit! l b)
    (.getText l) => "3__"
    (enter-digit! l b)
    (.getText l) => "33_"
    (enter-digit! l b)
    (.getText l) => "333"
    (enter-digit! l b)
    (.getText l) => "333"
    ))
