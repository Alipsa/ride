package tree;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import se.alipsa.ride.inout.FileItem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PseudoClassFileItem extends FileItem {

   public static final PseudoClass GIT_ADDED = PseudoClass.getPseudoClass("git-added");
   public static final PseudoClass GIT_UNTRACKED = PseudoClass.getPseudoClass("git-untracked");
   public static final PseudoClass GIT_CHANGED = PseudoClass.getPseudoClass("git-changed");

   List<PseudoClass> pseudoClasses = Arrays.asList(GIT_ADDED, GIT_UNTRACKED, GIT_CHANGED);

   private ObservableSet<PseudoClass> states;

   public PseudoClassFileItem(File file) {
      super(file);
      caption.pseudoClassStateChanged(GIT_ADDED, false);
      caption.pseudoClassStateChanged(GIT_UNTRACKED, false);
      caption.pseudoClassStateChanged(GIT_CHANGED, false);
      states = caption.getPseudoClassStates();
   }

   PseudoClass activePseudoClass = null;

   public PseudoClass getActivePseudoClass() {
      return activePseudoClass;
   }

   public void enablePseudoClass(PseudoClass pseudoClass) {
      System.out.println("setting active pseudoClass to " + pseudoClass);
      activePseudoClass = pseudoClass;
      for (PseudoClass pc : pseudoClasses) {
         if (pc.equals(pseudoClass)) {
            System.out.println("enabling " + pc);
            caption.pseudoClassStateChanged(pc, true);
         } else {
            System.out.println("disabling " + pc);
            caption.pseudoClassStateChanged(pc, false);
         }
      }
   }

   public List<PseudoClass> getPseudoClasses() {
      return pseudoClasses;
   }

   public void addPseudoClassChangeLister(SetChangeListener<PseudoClass> listener) {
      caption.getPseudoClassStates().addListener(listener);
      states.addListener(listener);
   }
}
