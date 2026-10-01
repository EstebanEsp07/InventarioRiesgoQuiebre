import { useCallback, useEffect, useState } from 'react'

export function useFetch<T>(fn: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState('')

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const load = useCallback(() => {
    return fn()
      .then((d) => {
        setData(d)
        setError('')
      })
      .catch((e: Error) => setError(e.message))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => {
    void load()
  }, [load])

  return { data, error, reload: load }
}
