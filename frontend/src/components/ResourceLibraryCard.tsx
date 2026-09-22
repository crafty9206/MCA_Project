import { useMemo, useState } from 'react'

type Resource = {
  title: string
  category: 'Nutrition' | 'Exercise' | 'Wellbeing' | 'Emergency'
  description: string
  linkLabel: string
}

const resources: Resource[] = [
  {
    title: 'Healthy eating through pregnancy',
    category: 'Nutrition',
    description: 'Simple guidance on balanced meals, hydration, and nutrition basics.',
    linkLabel: 'Learn more'
  },
  {
    title: 'Gentle movement ideas',
    category: 'Exercise',
    description: 'Walking, stretching, and low-impact habits that may support daily wellbeing.',
    linkLabel: 'View tips'
  },
  {
    title: 'Mindful wellbeing routines',
    category: 'Wellbeing',
    description: 'Supportive ideas for rest, stress management, and emotional balance.',
    linkLabel: 'Explore'
  },
  {
    title: 'Hospital and emergency checklist',
    category: 'Emergency',
    description: 'Helpful preparation steps and when to contact your care team urgently.',
    linkLabel: 'Review checklist'
  }
]

const categories = ['All', 'Nutrition', 'Exercise', 'Wellbeing', 'Emergency'] as const

export function ResourceLibraryCard() {
  const [selectedCategory, setSelectedCategory] = useState<(typeof categories)[number]>('All')
  const [search, setSearch] = useState('')

  const visibleResources = useMemo(() => {
    return resources.filter((resource) => {
      const categoryMatch = selectedCategory === 'All' || resource.category === selectedCategory
      const searchMatch = resource.title.toLowerCase().includes(search.toLowerCase()) || resource.description.toLowerCase().includes(search.toLowerCase())
      return categoryMatch && searchMatch
    })
  }, [search, selectedCategory])

  return (
    <section className="panel resource-panel" id="resources-library">
      <div className="panel-heading">
        <div>
          <p className="section-label">Resources</p>
          <h2>Helpful guides</h2>
        </div>
        <span className="task-count">{visibleResources.length} {visibleResources.length === 1 ? 'topic' : 'topics'}</span>
      </div>

      <p className="panel-description">Supportive information to revisit as your pregnancy changes.</p>

      <div className="resource-toolbar">
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search resources"
          aria-label="Search resources"
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
        {visibleResources.length === 0 ? (
          <div className="resource-empty">No matching resources found.</div>
        ) : (
          visibleResources.map((resource) => (
            <article className="resource-item" key={resource.title}>
              <span className="resource-category">{resource.category}</span>
              <strong>{resource.title}</strong>
              <p>{resource.description}</p>
              <button type="button">{resource.linkLabel} <span>→</span></button>
            </article>
          ))
        )}
      </div>
    </section>
  )
}
