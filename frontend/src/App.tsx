import { Button } from "@/components/ui/button.tsx";

function App() {

  return (
      <div className="flex min-h-screen items-center justify-center bg-background">
          <div className="text-center space-y-4">
              <h1 className="text-3xl font-bold text-foreground">
                  EmergencyWatch
              </h1>
              <p className="text-muted-foreground">
                  Frontend setup complete!
              </p>
              <Button>Test Button</Button>
          </div>
      </div>
  )
}

export default App
