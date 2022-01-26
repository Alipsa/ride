package se.alipsa.ride.utils.maven;

import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;

import java.io.File;
import java.util.*;

public class ModelResolver implements org.apache.maven.model.resolution.ModelResolver {

  List<RemoteRepository> remoteRepositories;
  RepositorySystemSession repositorySystemSession;
  RepositorySystem repositorySystem;
  VersionRangeResolver versionRangeResolver;
  String context = "project";
  private final Set<String> repositoryIds = new HashSet<>();
  private final RemoteRepositoryManager remoteRepositoryManager;

  public ModelResolver(List<RemoteRepository> remoteRepositories, RepositorySystemSession repositorySystemSession,
                       RepositorySystem repositorySystem) {
    this.remoteRepositories = remoteRepositories;
    this.repositorySystemSession = repositorySystemSession;
    this.repositorySystem = repositorySystem;
    this.versionRangeResolver = new DefaultVersionRangeResolver();
    this.remoteRepositoryManager = new DefaultRemoteRepositoryManager();
  }

  @Override
  public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
    Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "pom", version);
    ArtifactRequest artifactRequest = new ArtifactRequest(pomArtifact, remoteRepositories, context);
    try {
      ArtifactResult result = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
      File pomFile = result.getArtifact().getFile();
      return new FileModelSource( pomFile );
    } catch (Exception e) {
      throw new UnresolvableModelException("Failed to resolve model artifact", groupId, artifactId, version, e);
    }
  }

  @Override
  public ModelSource resolveModel( final Parent parent ) throws UnresolvableModelException {
    try {
      final Artifact artifact = new DefaultArtifact(
          parent.getGroupId(), parent.getArtifactId(), "", "pom", parent.getVersion()
      );
      final VersionRangeRequest versionRangeRequest = new VersionRangeRequest(
          artifact, remoteRepositories, context
      );
      final VersionRangeResult versionRangeResult =
          versionRangeResolver.resolveVersionRange( repositorySystemSession, versionRangeRequest );

      if ( versionRangeResult.getHighestVersion() == null ) {
        throw new UnresolvableModelException(
            String.format( "No versions matched the requested parent version range '%s'",
                parent.getVersion() ),
            parent.getGroupId(), parent.getArtifactId(), parent.getVersion() );
      }

      if ( versionRangeResult.getVersionConstraint() != null
          && versionRangeResult.getVersionConstraint().getRange() != null
          && versionRangeResult.getVersionConstraint().getRange().getUpperBound() == null ) {
        throw new UnresolvableModelException(
            String.format( "The requested parent version range '%s' does not specify an upper bound",
                parent.getVersion() ),
            parent.getGroupId(), parent.getArtifactId(), parent.getVersion() );

      }

      parent.setVersion( versionRangeResult.getHighestVersion().toString() );
      return resolveModel( parent.getGroupId(), parent.getArtifactId(), parent.getVersion() );
    }
    catch ( final VersionRangeResolutionException e ) {
      throw new UnresolvableModelException( e.getMessage(), parent.getGroupId(), parent.getArtifactId(),
          parent.getVersion(), e );
    }
  }

  @Override
  public void addRepository(Repository repository) throws InvalidRepositoryException {
    this.addRepository(repository, false);
  }

  @Override
  public void addRepository(Repository repository, boolean replace) {
    if (!repositorySystemSession.isIgnoreArtifactDescriptorRepositories()) {
      if (!this.repositoryIds.add(repository.getId())) {
        if (!replace) {
          return;
        }
        removeMatchingRepository(remoteRepositories, repository.getId());
      }
      List<RemoteRepository> newRepositories = Collections.singletonList(ArtifactDescriptorUtils.toRemoteRepository(repository));
      remoteRepositories = this.remoteRepositoryManager.aggregateRepositories(repositorySystemSession, remoteRepositories, newRepositories, true);
    }
  }

  @Override
  public org.apache.maven.model.resolution.ModelResolver newCopy() {
    return new ModelResolver(remoteRepositories, repositorySystemSession, repositorySystem);
  }

  private void removeMatchingRepository( Iterable<RemoteRepository> repositories, final String id ) {
    Iterator<RemoteRepository> iterator = repositories.iterator();
    while ( iterator.hasNext() )
    {
      RemoteRepository remoteRepository = iterator.next();
      if ( remoteRepository.getId().equals( id ) )
      {
        iterator.remove();
      }
    }
  }
}
