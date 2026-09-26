import { useDeferredValue, useEffect, useState } from 'react'
import { BookOpen, X } from 'lucide-react'
import { getResources } from '../api'
import type { EducationResource } from '../api'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

const categories = ['All', 'Nutrition', 'Exercise', 'Wellbeing', 'Postpartum', 'Newborn', 'Hospital', 'Emergency'] as const

export function ResourceLibraryCard({ language }: { language: LanguageCode }) {
  const [selectedCategory, setSelectedCategory] = useState<(typeof categories)[number]>('All')
  const [search, setSearch] = useState('')
  const deferredSearch = useDeferredValue(search)
  const [resources, setResources] = useState<EducationResource[]>([])
  const [selectedResource, setSelectedResource] = useState<EducationResource | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    let active = true
    setLoading(true)
    setError('')
    getResources(token, selectedCategory, deferredSearch)
      .then((loadedResources) => { if (active) setResources(loadedResources) })
      .catch(() => { if (active) setError('Resources could not be loaded. Please try again.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [deferredSearch, selectedCategory])

  return (
    <section className="panel resource-panel" id="resources-library">
      <div className="panel-heading">
        <div>
          <p className="section-label">{translate(language, 'resources.label')}</p>
          <h2>{translate(language, 'resources.title')}</h2>
        </div>
        <span className="task-count">{resources.length} {resources.length === 1 ? 'topic' : 'topics'}</span>
      </div>

      <p className="panel-description">{translate(language, 'resources.description')}</p>

      <div className="resource-toolbar">
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder={translate(language, 'resources.search')}
          aria-label={translate(language, 'resources.search')}
        />
        <div className="resource-filters" aria-label="Resource categories">
          {categories.map((category) => (
            <button
              key={category}
              type="button"
              className={selectedCategory === category ? 'active' : ''}
              onClick={() => setSelectedCategory(category)}
            >
              {category}
            </button>
          ))}
        </div>
      </div>

      <div className="resource-grid">
        {loading ? (
          <div className="resource-empty">{translate(language, 'resources.loading')}</div>
        ) : error ? (
          <div className="resource-empty form-error" role="alert">{error}</div>
        ) : resources.length === 0 ? (
          <div className="resource-empty">{translate(language, 'resources.empty')}</div>
        ) : (
          resources.map((resource) => (
            <article className="resource-item" key={resource.id}>
              <span className="resource-category">{resource.category}</span>
              <strong>{resource.title}</strong>
              <p>{resource.description}</p>
              <small className="resource-review">Educational review · v{resource.contentVersion}</small>
              <button type="button" onClick={() => setSelectedResource(resource)}>{translate(language, 'resources.open')} <span>→</span></button>
            </article>
          ))
        )}
      </div>
      {selectedResource && (
        <div className="confirm-overlay" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setSelectedResource(null) }}>
          <section className="resource-dialog" role="dialog" aria-modal="true" aria-labelledby="resource-dialog-title">
            <header><span className="resource-dialog-icon" aria-hidden="true"><BookOpen /></span><button type="button" aria-label="Close resource" onClick={() => setSelectedResource(null)}><X /></button></header>
            <p className="section-label">{selectedResource.category} · Educational content</p>
            <h2 id="resource-dialog-title">{selectedResource.title}</h2>
            <p className="resource-dialog-content">{selectedResource.content}</p>
            <ul>{selectedResource.keyPoints.map((point) => <li key={point}>{point}</li>)}</ul>
            <footer><span>Version {selectedResource.contentVersion} · Reviewed {new Date(`${selectedResource.reviewedOn}T00:00:00`).toLocaleDateString()}</span><strong>General education only. Discuss personal care decisions with your healthcare professional.</strong></footer>
          </section>
        </div>
      )}
    </section>
  )
}
