package se.alipsa.ride.utils.maven;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;

import java.io.File;
import java.util.List;

public class ModelResolver implements org.apache.maven.model.resolution.ModelResolver {

  List<RemoteRepository> remoteRepositories;
  RepositorySystemSession repositorySystemSession;
  RepositorySystem repositorySystem;

  public ModelResolver(List<RemoteRepository> remoteRepositories, RepositorySystemSession repositorySystemSession,
                       RepositorySystem repositorySystem) {
    this.remoteRepositories = remoteRepositories;
    this.repositorySystemSession = repositorySystemSession;
    this.repositorySystem = repositorySystem;
  }

  @Override
  public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
    Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "pom", version);
    ArtifactRequest artifactRequest = new ArtifactRequest(pomArtifact, remoteRepositories, "project");
    try {
      ArtifactResult result = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
      File pomFile = result.getArtifact().getFile();
      return new FileModelSource( pomFile );
    } catch (Exception e) {
      throw new UnresolvableModelException("Failed to resolve model artifact", groupId, artifactId, version, e);
    }
  }

  @Override
  public void addRepository(Repository repository) throws InvalidRepositoryException {
    // ignoring... artifact resolution via repository should already have happened before by maven core.
  }

  @Override
  public org.apache.maven.model.resolution.ModelResolver newCopy() {
    return new ModelResolver(remoteRepositories, repositorySystemSession, repositorySystem);
  }
}
