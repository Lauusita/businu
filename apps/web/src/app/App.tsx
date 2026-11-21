import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { ProveedorAvisos } from '../components/feedback/Avisos'
import { ApiError } from '../services/api/client'
import { AuthProvider } from './providers/AuthProvider'
import { router } from './router'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      // No se reintenta lo que no va a cambiar (permisos, validación, no encontrado, sesión)
      retry: (intentos, error) => !(error instanceof ApiError && error.status >= 400 && error.status < 500) && intentos < 1,
    },
  },
})

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ProveedorAvisos>
          <RouterProvider router={router} />
        </ProveedorAvisos>
      </AuthProvider>
    </QueryClientProvider>
  )
}
