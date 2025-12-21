import { Link } from "react-router-dom";

const NotFound = () => {
  let user = null;
  // Declare 'admin' variable here before the try-catch block
  let admin = null; 

  try {
    user = JSON.parse(localStorage.getItem("user"));
    // 'admin' variable is now correctly assigned here
    admin = JSON.parse(localStorage.getItem("admin")); 
  } catch {
    user = null;
    admin = null; // Optional: ensure admin is null if parsing fails
  }
  // Remove 'const role;' and declare/initialize it in one step
  let role; 

  if (!user && admin) role = "admin";
  else role = user?.role;

  const homeMap = {
    admin: "/admin/dashboard",
    student: "/student/dashboard",
    fa: "/fa/dashboard",
  };

  const home = homeMap[role] || "/login";

  const buttonText = role
    ? "Go to your dashboard"
    : "Go to login";

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        textAlign: "center",
        padding: "1rem",
      }}
    >
      <h1 style={{ fontSize: "3rem", marginBottom: "0.5rem" }}>404</h1>
      <h2 style={{ marginBottom: "0.5rem" }}>Page Not Found</h2>
      <p style={{ marginBottom: "1.5rem", color: "#666" }}>
        The page you are trying to access doesn't exist or was moved.
      </p>

      <Link
        to={home}
        style={{
          padding: "0.6rem 1.2rem",
          borderRadius: "6px",
          backgroundColor: "violet",
          color: "#fff",
          textDecoration: "none",
          fontWeight: 500,
        }}
      >
        {buttonText}
      </Link>
    </div>
  );
};

export default NotFound;
