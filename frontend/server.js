import express from "express"
import fs from "fs"

const app = express()
const PORT = 3000

const envJsPath = "./dist/env.js"
if (fs.existsSync(envJsPath)) {
  let content = fs.readFileSync(envJsPath, "utf8")
  content = content.replace(
    "__API_BASE_URL__",
    process.env.API_BASE_URL || ""
  )
  fs.writeFileSync(envJsPath, content)
}

app.use(express.static("dist"))
app.listen(PORT, () => console.log("Frontend running"))
