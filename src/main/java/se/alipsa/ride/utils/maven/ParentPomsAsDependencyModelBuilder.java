package se.alipsa.ride.utils.maven;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.*;

import java.util.*;

public class ParentPomsAsDependencyModelBuilder implements ModelBuilder {

   //private static final Logger log = LogManager.getLogger();

   private final DefaultModelBuilder delegate;

   public ParentPomsAsDependencyModelBuilder() {
      delegate = new DefaultModelBuilderFactory().newInstance();
   }

   @Override
   public ModelBuildingResult build(ModelBuildingRequest request) throws ModelBuildingException {
      return new ParentPomsAsDependencyModelBuildingResult(delegate.build(request));
   }

   @Override
   public ModelBuildingResult build(ModelBuildingRequest request, ModelBuildingResult result) throws ModelBuildingException {
      return delegate.build(request, result);
   }

   private static class ParentPomsAsDependencyModelBuildingResult implements ModelBuildingResult {

      private final ModelBuildingResult wrapped;

      public ParentPomsAsDependencyModelBuildingResult(ModelBuildingResult wrapped) {
         this.wrapped = wrapped;
      }

      @Override
      public Model getEffectiveModel() {
         Model original = wrapped.getEffectiveModel();
         Parent parent = original.getParent();
         if (parent != null) {
            Model clone = original.clone();
            Dependency parentDependency = new Dependency();
            parentDependency.setGroupId(parent.getGroupId());
            parentDependency.setArtifactId(parent.getArtifactId());
            parentDependency.setVersion(parent.getVersion());
            parentDependency.setScope("compile");
            parentDependency.setType("pom");
            clone.addDependency(parentDependency);
            return clone;
         } else {
            return original;
         }
      }

      @Override
      public List<String> getModelIds() {
         return wrapped.getModelIds();
      }

      @Override
      public Model getRawModel() {
         return wrapped.getRawModel();
      }

      @Override
      public Model getRawModel(String modelId) {
         return wrapped.getRawModel(modelId);
      }

      @Override
      public List<Profile> getActivePomProfiles(String modelId) {
         return wrapped.getActivePomProfiles(modelId);
      }

      @Override
      public List<Profile> getActiveExternalProfiles() {
         return wrapped.getActiveExternalProfiles();
      }

      @Override
      public List<ModelProblem> getProblems() {
         return wrapped.getProblems();
      }
   }
}
